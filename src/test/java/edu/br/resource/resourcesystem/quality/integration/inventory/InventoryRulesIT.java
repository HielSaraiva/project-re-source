package edu.br.resource.resourcesystem.quality.integration.inventory;

import static org.assertj.core.api.Assertions.*;

import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.dto.response.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.quality.support.WorkflowSupport;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

@DisplayName("RN-003/RN-022 — inventário próprio, edição, exclusão lógica e reenvio")
class InventoryRulesIT extends WorkflowSupport {
    NewDonationForm form() {
        var f = new NewDonationForm();
        f.setCategory(ItemCategory.ELECTRONICS);
        f.setTitle("Lote independente");
        f.setQuantity(10);
        f.setCondition(ItemCondition.GOOD);
        f.setDescription("Descrição do lote");
        f.setSubmissionKey(UUID.randomUUID());
        return f;
    }

    @Test
    void register_WithValidData_CreatesInventoryWithoutReservation() {
        int id = inventory.register(donor, form());
        var view = inventoryView.details(donor, id);
        assertThat(view.quantity()).isEqualTo(10);
        assertThat(view.availableQuantity()).isEqualTo(10);
        assertThat(view.status()).isEqualTo(DonationStatus.REGISTERED);
        assertThat(view.proposalCount()).isZero();
        assertThat(count("donation_creation_requests")).isEqualTo(1);
        assertThat(count("donation_status_history")).isEqualTo(1);
    }

    @Test
    void register_WithSameAttempt_ReusesId() {
        var f = form();
        assertThat(inventory.register(donor, f)).isEqualTo(inventory.register(donor, f));
        assertThat(count("donation_creation_requests")).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"title", "quantity", "condition", "description", "category"})
    void register_WithChangedProcessedAttempt_Rejects(String field) {
        var f = form();
        inventory.register(donor, f);
        switch (field) {
            case "title" -> f.setTitle("Outro lote");
            case "quantity" -> f.setQuantity(9);
            case "condition" -> f.setCondition(ItemCondition.NEW);
            case "description" -> f.setDescription("Outra descrição");
            default -> f.setCategory(ItemCategory.FOOD);
        }
        assertThatThrownBy(() -> inventory.register(donor, f))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(count("donation_creation_requests")).isEqualTo(1);
    }

    @Test
    void register_WithWhitespaceOnlyDifference_ReusesId() {
        var f = form();
        int id = inventory.register(donor, f);
        f.setTitle(" Lote independente ");
        f.setDescription(" Descrição do lote ");
        assertThat(inventory.register(donor, f)).isEqualTo(id);
    }

    @Test
    void register_WithNoAttemptKey_Rejects() {
        var f = form();
        f.setSubmissionKey(null);
        assertThatThrownBy(() -> inventory.register(donor, f))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void register_WithDeletedAttempt_DoesNotResurrect() {
        var f = form();
        int id = inventory.register(donor, f);
        inventory.delete(donor, id, 0);
        assertThatThrownBy(() -> inventory.register(donor, f))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(count("donation_creation_requests")).isEqualTo(1);
        assertThat(count("donations")).isEqualTo(1);
    }

    @Test
    void register_WithConcurrentIdenticalAttempt_CreatesOnce() throws Exception {
        var f = form();
        var gate = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var a =
                    pool.submit(
                            () -> {
                                gate.await();
                                return inventory.register(donor, f);
                            });
            var b =
                    pool.submit(
                            () -> {
                                gate.await();
                                return inventory.register(donor, f);
                            });
            gate.countDown();
            assertThat(a.get()).isEqualTo(b.get());
        }
        assertThat(count("donation_creation_requests")).isEqualTo(1);
    }

    @Test
    void register_WithNeedContext_ReusesSameProtectionAndItemType() {
        UUID key = UUID.randomUUID();
        var f =
                new RegisterDonationRequest(
                        necessity, "Contextual", 3, ItemCondition.USED, null, key);
        var created = workflow.register(donor, f);
        assertThat(workflow.register(donor, f).id()).isEqualTo(created.id());
        assertThat(
                        db.queryForObject(
                                "select i.item_type_id from donations d join packages p on"
                                        + " p.id=d.package_id join items i on i.id=p.item_id where"
                                        + " d.id=?",
                                Integer.class,
                                created.id()))
                .isEqualTo(type());
        assertThatThrownBy(
                        () ->
                                workflow.register(
                                        donor,
                                        new RegisterDonationRequest(
                                                necessity,
                                                "Mudou",
                                                3,
                                                ItemCondition.USED,
                                                null,
                                                key)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void update_WithCurrentVersion_UpdatesAggregateAndRejectsStaleForm() {
        inventory.update(donor, donation, edit(15, 0));
        var details = inventoryView.details(donor, donation);
        assertThat(details.itemName()).isEqualTo("Título atualizado");
        assertThat(details.quantity()).isEqualTo(15);
        assertThat(details.version()).isEqualTo(1);
        assertThatThrownBy(() -> inventory.update(donor, donation, edit(9, 0)))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> inventory.delete(donor, donation, 0))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(inventoryView.details(donor, donation).quantity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(
            value = Stage.class,
            names = {"PROPOSED", "ACCEPTED", "PERSONAL", "CARRIER", "REPORTED", "POSTED"})
    void manage_WithActiveProposal_BlocksEditAndDelete(Stage stage) {
        stage(stage);
        assertThatThrownBy(() -> inventory.update(donor, donation, edit(10, 0)))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> inventory.delete(donor, donation, 0))
                .isInstanceOf(ResponseStatusException.class);
        var view = inventoryView.details(donor, donation);
        assertThat(view.editable()).isFalse();
        assertThat(view.deletable()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(
            value = Stage.class,
            names = {"REJECTED", "CANCELLED"})
    void delete_WithOnlyReleasedProposals_ArchivesWithoutDestroyingProtocol(Stage state) {
        var p = stage(state);
        inventory.delete(donor, donation, 0);
        assertThat(count("matches")).isEqualTo(1);
        assertThat(count("match_status_history")).isGreaterThan(0);
        assertThat(
                        inventoryView
                                .findDonationsByDonor(
                                        donor,
                                        new DonationHistoryFilterRequest(null, null, null, 1))
                                .getTotalElements())
                .isZero();
        var archived =
                inventoryView.findDonationsByDonor(
                        donor, new DonationHistoryFilterRequest(null, null, null, 1, "archived"));
        assertThat(archived.getContent())
                .singleElement()
                .satisfies(
                        x -> {
                            assertThat(x.archived()).isTrue();
                            assertThat(x.editable()).isFalse();
                            assertThat(x.deletable()).isFalse();
                        });
        assertThat(inventoryView.proposals(donor, donation, 1).content())
                .extracting(DonationProposalResponse::protocol)
                .containsExactly(p.protocol());
        assertThat(pages.institutionDetail(ong, p.protocol())).containsKey("match");
        assertThatThrownBy(() -> propose(1)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> inventoryView.details(donor, donation))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void delete_WithNoProposals_RemovesAggregateOnly() {
        inventory.delete(donor, donation, 0);
        assertThat(count("donations")).isZero();
        assertThat(count("packages")).isZero();
        assertThat(count("items")).isZero();
        assertThat(count("item_types")).isEqualTo(1);
    }

    @Test
    void update_WithAlreadyReceivedQuantity_EnforcesFloorAndCanComplete() {
        stage(Stage.COMPLETED);
        assertThatThrownBy(() -> inventory.update(donor, donation, edit(3, 0)))
                .isInstanceOf(ResponseStatusException.class);
        inventory.update(donor, donation, edit(4, 0));
        assertThat(inventoryView.details(donor, donation).status())
                .isEqualTo(DonationStatus.COMPLETED);
        assertThat(
                        inventoryView
                                .findDonationsByDonor(
                                        donor,
                                        new DonationHistoryFilterRequest(null, null, null, 1))
                                .getTotalElements())
                .isZero();
        assertThat(
                        inventoryView
                                .findDonationsByDonor(
                                        donor,
                                        new DonationHistoryFilterRequest(
                                                null, null, null, 1, "completed"))
                                .getTotalElements())
                .isEqualTo(1);
        assertThatThrownBy(() -> inventory.update(donor, donation, edit(5, 1)))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> inventory.delete(donor, donation, 1))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void access_WithAnotherOwner_HidesInventoryAndProtocols() {
        stage(Stage.CANCELLED);
        assertThatThrownBy(() -> inventoryView.details(otherDonor, donation))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> inventoryView.proposals(otherDonor, donation, 1))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> inventory.update(otherDonor, donation, edit(10, 0)))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> inventory.delete(otherDonor, donation, 0))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void list_WithFiltersAndOutOfRange_PreservesOwnershipAndLastPage() {
        for (int i = 0; i < 12; i++) {
            var f = form();
            f.setTitle("Lote " + i);
            inventory.register(donor, f);
        }
        var f = form();
        inventory.register(otherDonor, f);
        var page =
                inventoryView.findDonationsByDonor(
                        donor,
                        new DonationHistoryFilterRequest(
                                null, ItemCategory.ELECTRONICS, DonationStatus.REGISTERED, 999));
        assertThat(page.getTotalElements()).isEqualTo(13);
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(4);
        assertThat(
                        inventoryView.findDonationsByDonor(
                                donor,
                                new DonationHistoryFilterRequest("inexistente", null, null, 1)))
                .isEmpty();
    }

    @Test
    void list_WithLiteralWildcard_DoesNotMatchEverything() {
        var f = form();
        f.setTitle("Item % especial");
        int id = inventory.register(donor, f);
        assertThat(
                        inventoryView
                                .findDonationsByDonor(
                                        donor, new DonationHistoryFilterRequest("%", null, null, 1))
                                .getContent())
                .extracting(DonationHistoryResponse::id)
                .containsExactly(id);
    }

    @Test
    void proposals_WithManyInstitutionsAndTerminalStates_PaginatesAllProtocols() {
        inventory.update(donor, donation, edit(30, 0));
        for (int i = 0; i < 11; i++) {
            int n = needFor(i % 2 == 0 ? ong : otherOng, type(), 10);
            var p = workflow.propose(donor, new SubmitProposalRequest(n, donation, 1, null));
            if (i % 2 == 0) workflow.cancel(donor, p.protocol());
            else workflow.reject(otherOng, p.protocol(), "Sem necessidade");
        }
        assertThat(inventoryView.proposals(donor, donation, 1).content()).hasSize(8);
        assertThat(inventoryView.proposals(donor, donation, 999).content()).hasSize(3);
        assertThat(
                        inventoryView
                                .proposals(donor, donation, 1, MatchStatus.CANCELLED)
                                .totalElements())
                .isEqualTo(6);
        assertThatThrownBy(() -> inventoryView.proposals(donor, donation, 0))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> inventoryView.proposals(donor, donation, 1, MatchStatus.PROPOSED))
                .isInstanceOf(ResponseStatusException.class);
    }
}

package edu.br.resource.resourcesystem.quality.integration.necessity;

import static org.assertj.core.api.Assertions.*;

import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.dto.response.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.quality.support.WorkflowSupport;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

@DisplayName("RN-002/RN-021 — gestão da necessidade sem violar compromissos")
class NecessityRulesIT extends WorkflowSupport {
    ManageNecessityRequest request(String name, int quantity, long version) {
        return new ManageNecessityRequest(
                ItemCategory.ELECTRONICS,
                name,
                BigDecimal.valueOf(quantity),
                NecessityPriority.HIGH,
                version);
    }

    @Test
    void create_WithNormalizedCatalog_ReusesTypeWithoutMutatingOthers() {
        needs.create(ong, request(" Equipamento de teste ", 5, 0));
        needs.create(otherOng, request("EQUIPAMENTO DE TESTE", 6, 0));
        assertThat(count("item_types")).isEqualTo(1);
        assertThat(count("necessities")).isEqualTo(3);
    }

    @Test
    void update_WithCurrentVersion_DoesNotMutateSharedType() {
        needs.update(ong, necessity, request("Outro equipamento", 8, 0));
        assertThat(db.queryForObject("select title from items limit 1", String.class))
                .isEqualTo("Equipamento oferecido");
        assertThat(count("item_types")).isEqualTo(2);
        var after = needs.editDetails(ong, necessity);
        assertThat(after.version()).isEqualTo(1);
        assertThat(after.quantity()).isEqualTo(8);
        assertThatThrownBy(() -> needs.update(ong, necessity, request("Antigo", 9, 0)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void access_WithOtherInstitution_HidesNeed() {
        assertThatThrownBy(() -> needs.editDetails(otherOng, necessity))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> needs.update(otherOng, necessity, request("Inválido", 1, 0)))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> needs.delete(otherOng, necessity, 0))
                .isInstanceOf(ResponseStatusException.class);
    }

    @ParameterizedTest
    @EnumSource(
            value = Stage.class,
            names = {
                "PROPOSED",
                "ACCEPTED",
                "PERSONAL",
                "CARRIER",
                "REPORTED",
                "POSTED",
                "COMPLETED"
            })
    void manage_WithActiveOrCompletedProposal_BlocksBothActions(Stage state) {
        stage(state);
        assertThatThrownBy(() -> needs.update(ong, necessity, request("Troca indevida", 20, 0)))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> needs.delete(ong, necessity, 0))
                .isInstanceOf(ResponseStatusException.class);
        var view = needs.editDetails(ong, necessity);
        assertThat(view.editable()).isFalse();
        assertThat(view.deletable()).isFalse();
        assertThat(view.restrictionReason()).isNotBlank();
    }

    @ParameterizedTest
    @EnumSource(
            value = Stage.class,
            names = {"REJECTED", "CANCELLED"})
    void delete_WithReleasedHistory_PreservesProtocolAndHidesNeed(Stage state) {
        var p = stage(state);
        needs.delete(ong, necessity, 0);
        assertThat(count("matches")).isEqualTo(1);
        assertThatThrownBy(() -> needs.editDetails(ong, necessity))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(needs.list(ong, new NecessityFilterRequest(null, null, null, null, 1, 9)))
                .isEmpty();
        assertThat(pages.institutionDetail(ong, p.protocol())).containsKey("match");
        assertThatThrownBy(() -> propose(1)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void delete_WithNoProposals_RemovesNeedOnly() {
        needs.delete(ong, necessity, 0);
        assertThat(count("necessities")).isZero();
        assertThat(count("items")).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(
            value = NecessityStatus.class,
            names = {"FULFILLED", "CANCELLED"})
    void manage_WithClosedNeed_RejectsEvenWithoutProposal(NecessityStatus status) {
        db.update(
                "update necessities set status=?::necessity_status where id=?",
                status.getValue(),
                necessity);
        assertThatThrownBy(() -> needs.update(ong, necessity, request("Alterada", 8, 0)))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> needs.delete(ong, necessity, 0))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void update_WithMissingOrOldVersion_Rejects() {
        assertThatThrownBy(
                        () ->
                                needs.update(
                                        ong,
                                        necessity,
                                        new ManageNecessityRequest(
                                                ItemCategory.ELECTRONICS,
                                                "Outro",
                                                BigDecimal.ONE,
                                                NecessityPriority.HIGH,
                                                null)))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> needs.delete(ong, necessity, 4))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void list_WithReservationsAndReceipt_SeparatesRequestedReservedReceived() {
        var p = propose(4);
        var before = needs.editDetails(ong, necessity);
        assertThat(before.quantity()).isEqualTo(10);
        assertThat(before.reservedQuantity()).isEqualTo(4);
        assertThat(before.receivedQuantity()).isZero();
        assertThat(before.remainingQuantity()).isEqualTo(6);
        workflow.cancel(donor, p.protocol());
        assertThat(needs.editDetails(ong, necessity).remainingQuantity()).isEqualTo(10);
    }

    @Test
    void list_WithCategoryPriorityTextAndPage_PreservesScope() {
        for (int i = 0; i < 10; i++) needs.create(ong, request("Lista " + i, 5, 0));
        needs.create(otherOng, request("Lista de outra ONG", 5, 0));
        var filter =
                new NecessityFilterRequest(
                        "Lista",
                        ItemCategory.ELECTRONICS,
                        NecessityPriority.HIGH,
                        NecessityStatus.ACTIVE,
                        999,
                        9);
        var page = needs.list(ong, filter);
        assertThat(page.getTotalElements()).isEqualTo(10);
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(needs.list(ong, new NecessityFilterRequest("ausente", null, null, null, 1, 9)))
                .isEmpty();
    }

    @Test
    void create_WithConcurrentCatalogVariants_KeepsUniqueIdentity() throws Exception {
        var gate = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var a =
                    pool.submit(
                            () -> {
                                gate.await();
                                needs.create(ong, request("Novo catálogo", 5, 0));
                                return true;
                            });
            var b =
                    pool.submit(
                            () -> {
                                gate.await();
                                needs.create(otherOng, request(" NOVO CATÁLOGO ", 5, 0));
                                return true;
                            });
            gate.countDown();
            assertThat(a.get()).isTrue();
            assertThat(b.get()).isTrue();
        }
        assertThat(
                        db.queryForObject(
                                "select count(*) from item_types where lower(btrim(name))='novo"
                                        + " catálogo'",
                                Long.class))
                .isEqualTo(1);
    }
}

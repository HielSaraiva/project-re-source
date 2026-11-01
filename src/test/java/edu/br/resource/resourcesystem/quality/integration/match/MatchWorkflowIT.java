package edu.br.resource.resourcesystem.quality.integration.match;

import static org.assertj.core.api.Assertions.*;

import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.dto.response.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.quality.support.WorkflowSupport;
import edu.br.resource.resourcesystem.service.match.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

@DisplayName("RN-002 a RN-015 — máquina de estados, reservas e rastreabilidade")
class MatchWorkflowIT extends WorkflowSupport {
    static Stream<Arguments> transitions() {
        return Arrays.stream(Stage.values())
                .flatMap(
                        s ->
                                Stream.of(
                                                "accept",
                                                "reject",
                                                "cancel",
                                                "method",
                                                "personal",
                                                "shipment",
                                                "receive")
                                        .map(a -> Arguments.of(s, a)));
    }

    boolean allowed(Stage s, String action) {
        return switch (action) {
            case "accept", "reject" -> s == Stage.PROPOSED;
            case "cancel" ->
                    Set.of(Stage.PROPOSED, Stage.ACCEPTED, Stage.PERSONAL, Stage.CARRIER)
                            .contains(s);
            case "method" -> s == Stage.ACCEPTED;
            case "personal" -> s == Stage.PERSONAL;
            case "shipment" -> s == Stage.CARRIER;
            case "receive" -> s == Stage.REPORTED || s == Stage.POSTED;
            default -> false;
        };
    }

    @ParameterizedTest(name = "{0} → {1}")
    @MethodSource("transitions")
    @DisplayName(
            "Dado uma etapa, quando executada uma ação, então deve respeitar a transição permitida"
                    + " pela RN")
    void transition_WithStageAndAction_PreservesStateMachine(Stage stage, String action) {
        var before = stage(stage);
        long events = count("match_status_history");
        if (allowed(stage, action)) {
            var result = action(action, before.protocol());
            assertThat(result.protocol()).isEqualTo(before.protocol());
            assertThat(count("match_status_history")).isEqualTo(events + 1);
        } else {
            assertThatThrownBy(() -> action(action, before.protocol()))
                    .isInstanceOfSatisfying(
                            ResponseStatusException.class,
                            e ->
                                    assertThat(e.getStatusCode().value())
                                            .isEqualTo(
                                                    stage == Stage.ACCEPTED
                                                                    && action.equals("shipment")
                                                            ? 404
                                                            : 409));
            assertThat(count("match_status_history")).isEqualTo(events);
            assertThat(
                            db.queryForObject(
                                    "select status::text from matches where protocol=?",
                                    String.class,
                                    before.protocol()))
                    .isEqualTo(before.status().getValue());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10})
    void propose_WithValidQuantity_ReservesBothSides(int quantity) {
        var result = propose(quantity);
        assertThat(result.status()).isEqualTo(MatchStatus.AWAITING_ACCEPTANCE);
        assertThat(result.donation().availableQuantity()).isEqualTo(10 - quantity);
        assertThat(result.need().remainingQuantity()).isEqualTo(10 - quantity);
        assertThat(result.deadlines().activeDeadline())
                .isBetween(
                        Instant.now().plus(Duration.ofDays(6)),
                        Instant.now().plus(Duration.ofDays(8)));
        assertThat(count("match_status_history")).isEqualTo(1);
        assertThat(count("match_event_outbox")).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 11, Integer.MAX_VALUE})
    void propose_WithInvalidQuantity_DoesNotAllocate(int quantity) {
        assertThatThrownBy(() -> propose(quantity)).isInstanceOf(RuntimeException.class);
        assertThat(count("matches")).isZero();
    }

    @Test
    void propose_WithOtherDonorInventory_ReturnsNotFound() {
        assertThatThrownBy(
                        () ->
                                workflow.propose(
                                        otherDonor,
                                        new SubmitProposalRequest(necessity, donation, 1, null)))
                .isInstanceOfSatisfying(
                        ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void propose_WithOtherCategory_Rejects() {
        db.update("update item_types set category='food' where id=?", type());
        int t =
                db.queryForObject(
                        "insert into item_types(category,name) values('electronics','Outra"
                                + " categoria') returning id",
                        Integer.class);
        int d = stock(donor, t, 10, "Outro item");
        assertThatThrownBy(
                        () ->
                                workflow.propose(
                                        donor, new SubmitProposalRequest(necessity, d, 1, null)))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(count("matches")).isZero();
    }

    @ParameterizedTest
    @EnumSource(
            value = InstitutionStatus.class,
            names = {"PENDING_APPROVAL", "REJECTED", "BLOCKED"})
    void propose_WithUnavailableInstitution_Rejects(InstitutionStatus state) {
        db.update(
                "update institutions set status=?::institution_status,rejection_reason='Motivo"
                        + " fictício' where id=?",
                state.getValue(),
                ong);
        assertThatThrownBy(() -> propose(1)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void propose_WithDeletedInventory_Rejects() {
        db.update("update donations set deleted_at=now() where id=?", donation);
        assertThatThrownBy(() -> propose(1)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void propose_WithDeletedNeed_Rejects() {
        db.update("update necessities set deleted_at=now() where id=?", necessity);
        assertThatThrownBy(() -> propose(1)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void propose_WithActiveDuplicate_RejectsWithoutDoubleReservation() {
        propose(3);
        assertThatThrownBy(() -> propose(2)).isInstanceOf(ResponseStatusException.class);
        assertThat(count("matches")).isEqualTo(1);
        assertThat(inventoryView.details(donor, donation).reservedQuantity()).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(strings = {"reject", "cancel"})
    void release_WithClosedProposal_AllowsNewProposal(String action) {
        var p = propose(4);
        action(action, p.protocol());
        assertThat(propose(10).allocatedQuantity()).isEqualTo(10);
        assertThat(count("matches")).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"acceptance", "choice", "in_person", "carrier"})
    void expire_WithExpiredStage_CancelsAndReleasesExactlyOnce(String expired) {
        Stage s =
                switch (expired) {
                    case "acceptance" -> Stage.PROPOSED;
                    case "choice" -> Stage.ACCEPTED;
                    case "in_person" -> Stage.PERSONAL;
                    default -> Stage.CARRIER;
                };
        var p = stage(s);
        if (expired.equals("acceptance"))
            db.update(
                    "update matches set acceptance_deadline=now()-interval '1 second' where id=?",
                    p.id());
        else if (expired.equals("choice"))
            db.update(
                    "update matches set delivery_method_deadline=now()-interval '1 second' where"
                            + " id=?",
                    p.id());
        else
            db.update(
                    "update deliveries set "
                            + (expired.equals("carrier")
                                    ? "shipping_deadline"
                                    : "in_person_deadline")
                            + "=now()-interval '1 second' where match_id=?",
                    p.id());
        assertThat(workflow.expire(p.protocol())).isTrue();
        long count = count("match_status_history");
        assertThat(workflow.expire(p.protocol())).isFalse();
        assertThat(count("match_status_history")).isEqualTo(count);
        assertThat(inventoryView.details(donor, donation).availableQuantity()).isEqualTo(10);
        assertThat(
                        db.queryForObject(
                                "select expired_stage from matches where id=?",
                                String.class,
                                p.id()))
                .isEqualTo(expired);
    }

    @ParameterizedTest
    @EnumSource(
            value = Stage.class,
            names = {"REPORTED", "POSTED", "COMPLETED", "CANCELLED", "REJECTED"})
    void expire_WithNoActiveDeadline_DoesNotCancel(Stage stage) {
        var p = stage(stage);
        assertThat(workflow.expire(p.protocol())).isFalse();
    }

    @Test
    void accept_WithExpiredDeadline_CommitsCancellationDespiteConflict() {
        var p = propose(4);
        db.update(
                "update matches set acceptance_deadline=now()-interval '1 second' where id=?",
                p.id());
        assertThatThrownBy(() -> workflow.accept(ong, p.protocol()))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(inventoryView.details(donor, donation).availableQuantity()).isEqualTo(10);
        assertThat(
                        db.queryForObject(
                                "select status::text from matches where id=?",
                                String.class,
                                p.id()))
                .isEqualTo("cancelled");
    }

    @ParameterizedTest
    @EnumSource(DeliveryMethod.class)
    void chooseMethod_WithMissingAddress_Rejects(DeliveryMethod method) {
        var p = stage(Stage.ACCEPTED);
        db.update("delete from institution_addresses where institution_id=?", ong);
        assertThatThrownBy(() -> workflow.selectMethod(donor, p.protocol(), method))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(count("deliveries")).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 1})
    void reportDelivery_WithDateOutsideSelectedStage_Rejects(int days) {
        var p = stage(Stage.PERSONAL);
        assertThatThrownBy(
                        () ->
                                workflow.reportDelivery(
                                        donor,
                                        p.protocol(),
                                        new ConfirmInPersonDeliveryRequest(
                                                today().plusDays(days), "Pessoa", null, true)))
                .isInstanceOf(RuntimeException.class);
        assertThat(count("match_status_history")).isEqualTo(3);
    }

    @Test
    void receive_WithWholeNeed_ClosesNeedAndDonation() {
        var p = propose(10);
        workflow.accept(ong, p.protocol());
        workflow.selectMethod(donor, p.protocol(), DeliveryMethod.CARRIER);
        workflow.reportShipment(
                donor,
                p.protocol(),
                new ConfirmCarrierShipmentRequest(today(), "ab123456789br", "Nota", true));
        var completed = workflow.receive(ong, p.protocol());
        assertThat(completed.status()).isEqualTo(MatchStatus.COMPLETED);
        assertThat(completed.need().status()).isEqualTo(NecessityStatus.FULFILLED);
        assertThat(inventoryView.details(donor, donation).status())
                .isEqualTo(DonationStatus.COMPLETED);
        assertThat(completed.delivery().trackingCode()).isEqualTo("AB123456789BR");
    }

    @Test
    void receive_WithPartialNeed_KeepsRemainderAndHistoricalItem() {
        var p = stage(Stage.COMPLETED);
        assertThat(inventoryView.details(donor, donation).availableQuantity()).isEqualTo(6);
        inventory.update(donor, donation, edit(10, 0));
        var view = (MatchDetailResponse) pages.institutionDetail(ong, p.protocol()).get("match");
        assertThat(view.donation().item()).isEqualTo("Equipamento oferecido");
        assertThat(view.donation().condition()).isEqualTo(ItemCondition.GOOD);
        assertThat(view.delivery().receivedByName()).isEqualTo("Recebedora fictícia");
        assertThat(view.history())
                .filteredOn(e -> e.type() == MatchEventType.DELIVERY_REPORTED)
                .allSatisfy(e -> assertThat(e.description()).contains("Recebedora fictícia"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"accept", "reject", "receive"})
    void operation_WithAnotherInstitution_ReturnsNotFound(String action) {
        var p = stage(Stage.PROPOSED);
        assertThatThrownBy(
                        () -> {
                            switch (action) {
                                case "accept" -> workflow.accept(otherOng, p.protocol());
                                case "reject" -> workflow.reject(otherOng, p.protocol(), "Motivo");
                                default -> workflow.receive(otherOng, p.protocol());
                            }
                        })
                .isInstanceOfSatisfying(
                        ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void propose_WithConcurrentDonors_DoesNotOverAllocate() throws Exception {
        int other = stock(otherDonor, type(), 10, "Segundo lote");
        var gate = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var a =
                    pool.submit(
                            () -> {
                                gate.await();
                                try {
                                    workflow.propose(
                                            donor,
                                            new SubmitProposalRequest(
                                                    necessity, donation, 7, null));
                                    return 200;
                                } catch (ResponseStatusException e) {
                                    return e.getStatusCode().value();
                                }
                            });
            var b =
                    pool.submit(
                            () -> {
                                gate.await();
                                try {
                                    workflow.propose(
                                            otherDonor,
                                            new SubmitProposalRequest(necessity, other, 7, null));
                                    return 200;
                                } catch (ResponseStatusException e) {
                                    return e.getStatusCode().value();
                                }
                            });
            gate.countDown();
            assertThat(Stream.of(a.get(), b.get()).sorted().toList()).containsExactly(200, 409);
        }
        assertThat(
                        db.queryForObject(
                                "select sum(allocated_quantity) from matches where necessity_id=?",
                                Long.class,
                                necessity))
                .isEqualTo(7);
    }
}

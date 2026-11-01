package edu.br.resource.resourcesystem.quality.integration.match;

import static org.assertj.core.api.Assertions.*;

import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.quality.support.WorkflowSupport;
import edu.br.resource.resourcesystem.repository.MatchDeadlineRepository;
import edu.br.resource.resourcesystem.service.match.MatchDeadlineScheduler;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

@DisplayName("RN-008/RN-011/RN-012/RN-013 — coerência da entrega e processamento de prazo")
class WorkflowIntegrityIT extends WorkflowSupport {
    @Autowired MatchDeadlineRepository deadlineRepository;
    @Autowired MatchDeadlineScheduler scheduler;

    @Test
    void report_WithAlreadyReportedPendingDelivery_RejectsDuplicate() {
        var p = stage(Stage.PERSONAL);
        db.update("update deliveries set reported_at=now() where match_id=?", p.id());
        assertThatThrownBy(
                        () ->
                                workflow.reportDelivery(
                                        donor,
                                        p.protocol(),
                                        new ConfirmInPersonDeliveryRequest(
                                                today(), "Pessoa", null, true)))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(count("match_status_history")).isEqualTo(3);
    }

    @Test
    void receive_WithInconsistentDeliveryStatus_DoesNotConsumeReservation() {
        var p = stage(Stage.REPORTED);
        db.update("update deliveries set status='cancelled' where match_id=?", p.id());
        assertThatThrownBy(() -> workflow.receive(ong, p.protocol()))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(inventoryView.details(donor, donation).receivedQuantity()).isZero();
        assertThat(inventoryView.details(donor, donation).reservedQuantity()).isEqualTo(4);
    }

    @Test
    void report_WithCancelledCarrierDelivery_Rejects() {
        var p = stage(Stage.CARRIER);
        db.update("update deliveries set status='cancelled' where match_id=?", p.id());
        assertThatThrownBy(
                        () ->
                                workflow.reportShipment(
                                        donor,
                                        p.protocol(),
                                        new ConfirmCarrierShipmentRequest(
                                                today(), "AB123456789BR", null, true)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void scheduler_WithExpiredAndLiveProposals_CancelsOnlyExpired() {
        var p = propose(3);
        int n = needFor(ong, type(), 10);
        var live = workflow.propose(donor, new SubmitProposalRequest(n, donation, 2, null));
        db.update(
                "update matches set acceptance_deadline=now()-interval '1 second' where id=?",
                p.id());
        assertThat(deadlineRepository.findExpiredProtocols()).containsExactly(p.protocol());
        scheduler.cancelExpired();
        assertThat(
                        db.queryForObject(
                                "select status::text from matches where id=?",
                                String.class,
                                p.id()))
                .isEqualTo("cancelled");
        assertThat(
                        db.queryForObject(
                                "select status::text from matches where id=?",
                                String.class,
                                live.id()))
                .isEqualTo("awaiting_acceptance");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "x"})
    void history_WithOptionalDeliveryNotes_PreservesRecipientWithoutInventingNotes(String note) {
        var p = stage(Stage.PERSONAL);
        workflow.reportDelivery(
                donor,
                p.protocol(),
                new ConfirmInPersonDeliveryRequest(today(), " Pessoa fictícia ", note, true));
        var model =
                (edu.br.resource.resourcesystem.dto.response.MatchDetailResponse)
                        pages.institutionDetail(ong, p.protocol()).get("match");
        assertThat(model.delivery().receivedByName()).isEqualTo("Pessoa fictícia");
        assertThat(model.delivery().notes()).isEqualTo(note.isBlank() ? null : note);
    }
}

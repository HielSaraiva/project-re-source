package edu.br.resource.resourcesystem.service.donor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import edu.br.resource.resourcesystem.model.view.DonationHistoryEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.br.resource.resourcesystem.model.view.DonationDetail;
import org.springframework.stereotype.Service;

@Service
public class DonorDonationStatusService {
    public Map<String, Object> screenData() {
        return screenData(false);
    }

    public Map<String, Object> screenData(boolean rejected) {
        var deadline = MockDonationDeadline.recent();
        var model = new LinkedHashMap<String, Object>();
        model.put("rejected", rejected);
        model.put("rejectedAt", deadline.startedInstant());
        model.put("rejectedAtLabel", deadline.startedOnLabel() + " às 00:00");
        model.put("rejectionReason", "A instituição já recebeu os monitores necessários para esta necessidade. Agradecemos sua disponibilidade para ajudar.");
        model.put("acceptanceDeadline", deadline.deadlineInstant());
        model.put("donorName", "João Silva");
        var donation = new Donation(rejected ? MockDonationProtocols.REJECTED_ASSOCIACAO_VIDA : MockDonationProtocols.PENDING_ASSOCIACAO_VIDA,
                "1x Monitor Dell 24\"", "Associação Vida", rejected ? deadline.startedOn().minusDays(2).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : deadline.startedOnLabel());
        model.put("donation", donation);
        var proposed = MockDonationHistory.proposed(LocalDate.parse(donation.donationDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.put("donationHistory", rejected ? List.of(proposed,
                new DonationHistoryEvent("Proposta recusada", deadline.startedInstant(), donation.organization(),
                        (String) model.get("rejectionReason"))) : List.of(proposed));
        model.put("donationDetails", List.of(
                new DonationDetail("gift.svg", "Item Doado", "1x Monitor Dell 24\""),
                new DonationDetail("tag.svg", "Categoria", "Eletrônicos"),
                new DonationDetail("check-circle.svg", "Condição", "Usado (Bom estado)"),
                new DonationDetail("calendar.svg", "Data da Doação", donation.donationDate())));
        model.put("organizationDetails", List.of(
                new DonationDetail("user.svg", "Destinatário", "Associação Vida"),
                new DonationDetail("file-text.svg", "CNPJ", "12.345.678/0001-90"),
                new DonationDetail("mail.svg", "Contato", "contato@associacaovida.org"),
                rejected ? new DonationDetail("clock.svg", "Recusa registrada em", deadline.startedOnLabel() + " às 00:00")
                        : new DonationDetail("clock.svg", "Prazo para Aceite", "Até " + deadline.deadlineLabel() + " (7 dias após a proposta)")));
        return model;
    }

    public record Donation(String protocol, String item, String organization, String donationDate) {
    }
}

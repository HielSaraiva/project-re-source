package edu.br.resource.resourcesystem.service.donor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.br.resource.resourcesystem.model.view.DonationDetail;
import org.springframework.stereotype.Service;

@Service
public class DonorDonationStatusService {
    public Map<String, Object> screenData() {
        var deadline = MockDonationDeadline.recent();
        var model = new LinkedHashMap<String, Object>();
        model.put("acceptanceDeadline", deadline.deadlineInstant());
        model.put("donorName", "João Silva");
        model.put("donation", new Donation(
                "1x Monitor Dell 24\"", "Associação Vida", deadline.startedOnLabel()));
        model.put("donationDetails", List.of(
                new DonationDetail("gift.svg", "Item Doado", "1x Monitor Dell 24\""),
                new DonationDetail("tag.svg", "Categoria", "Eletrônicos"),
                new DonationDetail("check-circle.svg", "Condição", "Usado (Bom estado)"),
                new DonationDetail("calendar.svg", "Data da Doação", deadline.startedOnLabel())));
        model.put("organizationDetails", List.of(
                new DonationDetail("user.svg", "Destinatário", "Associação Vida"),
                new DonationDetail("file-text.svg", "CNPJ", "12.345.678/0001-90"),
                new DonationDetail("mail.svg", "Contato", "contato@associacaovida.org"),
                new DonationDetail("clock.svg", "Prazo para Aceite", "Até " + deadline.deadlineLabel() + " (7 dias após a proposta)")));
        return model;
    }

    public record Donation(String item, String organization, String donationDate) {
    }
}

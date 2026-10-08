package edu.br.resource.resourcesystem.service.donor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import edu.br.resource.resourcesystem.model.view.DonationDetail;
import org.springframework.stereotype.Service;

@Service
public class DonorInPersonDeliveryService {


    public Map<String, Object> deliveryData() {
        var recipient = MockDonationRecipient.institutoEsperanca();
        var today = LocalDate.now(ZoneId.of("America/Fortaleza"));
        var timeline = MockDonationDeadline.selectedToday();
        return Map.of(
                "donorName", "João Silva",
                "donationHistory", MockDonationHistory.accepted(MockDonationDeadline.recent(), recipient.name()),
                "donation", new Donation(MockDonationProtocols.DELIVERY_INSTITUTO_ESPERANCA, "1x Monitor Dell 24\"", "Instituto Esperança", "Eletrônicos", "Usado (Bom estado)"),
                "receivingDetails", List.of(
                        new DonationDetail("map.svg", "Local de entrega", recipient.address() + ", " + recipient.complement() + ", CEP " + recipient.postalCode()),
                        new DonationDetail("clock.svg", "Horário de recebimento", "Segunda a sexta, das 08:00 às 17:00"),
                        new DonationDetail("user.svg", "Responsável pelo recebimento", recipient.contactName())),
                "methodConfirmedOn", timeline.startedOn().toString(),
                "methodConfirmedOnLabel", timeline.startedOnLabel(),
                "today", today.toString(),
                "deliveryDeadline", timeline.deadline().toString(),
                "deliveryDeadlineLabel", timeline.deliveryLabel());
    }

    public record Donation(String protocol, String item, String organization, String category, String condition) {}
}

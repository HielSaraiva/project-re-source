package edu.br.resource.resourcesystem.service.donor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class DonorCarrierDeliveryService {
    public Map<String, Object> deliveryData() {
        var deadline = MockDonationDeadline.selectedToday();
        var recipient = MockDonationRecipient.institutoEsperanca();
        var today = LocalDate.now(ZoneId.of("America/Fortaleza"));
        return Map.of(
                "donorName", "João Silva",
                "donationHistory", MockDonationHistory.accepted(MockDonationDeadline.recent(), recipient.name()),
                "donation", new Donation(MockDonationProtocols.DELIVERY_INSTITUTO_ESPERANCA, "1x Monitor Dell 24\"", recipient.name()),
                "recipientDetails", recipient.postalDetails(),
                "methodConfirmedOn", deadline.startedOn().toString(),
                "methodConfirmedOnLabel", deadline.startedOnLabel(),
                "deliveryDeadline", deadline.deadline().toString(),
                "deliveryDeadlineLabel", deadline.deliveryLabel(),
                "today", today.toString());
    }

    public record Donation(String protocol, String item, String organization) {}
}

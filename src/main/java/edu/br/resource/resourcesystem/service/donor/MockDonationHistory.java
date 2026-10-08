package edu.br.resource.resourcesystem.service.donor;

import edu.br.resource.resourcesystem.model.view.DonationHistoryEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

final class MockDonationHistory {
    private MockDonationHistory() {}

    static DonationHistoryEvent proposed(LocalDate date) {
        return new DonationHistoryEvent("Proposta enviada", instant(date), "João Silva",
                "Itens e quantidade oferecidos à ONG. A reserva foi registrada para esta proposta.");
    }

    static List<DonationHistoryEvent> accepted(MockDonationDeadline deadline, String organization) {
        return List.of(proposed(deadline.startedOn().minusDays(1)),
                new DonationHistoryEvent("Proposta aceita", deadline.startedInstant(), organization,
                        "Proposta aceita pela ONG. Iniciado o prazo de 7 dias para escolher a modalidade de entrega."));
    }

    static String instant(LocalDate date) {
        return date.atStartOfDay(ZoneId.of("America/Fortaleza")).toInstant().toString();
    }
}

package edu.br.resource.resourcesystem.service.donor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Prazo de apresentação da PoC, contado a partir do início de cada etapa. */
record MockDonationDeadline(LocalDate startedOn, LocalDate deadline) {
    private static final int DEFAULT_DAYS = 7;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    static MockDonationDeadline recent() {
        var startedOn = LocalDate.now(ZoneId.of("America/Fortaleza")).minusDays(1);
        return new MockDonationDeadline(startedOn, startedOn.plusDays(DEFAULT_DAYS));
    }

    static MockDonationDeadline expired() {
        var startedOn = LocalDate.now(ZoneId.of("America/Fortaleza")).minusDays(8);
        return new MockDonationDeadline(startedOn, startedOn.plusDays(DEFAULT_DAYS));
    }

    String startedInstant() {
        return startedOn.atStartOfDay(ZoneId.of("America/Fortaleza")).toInstant().toString();
    }

    static MockDonationDeadline selectedToday() {
        var today = LocalDate.now(ZoneId.of("America/Fortaleza"));
        return new MockDonationDeadline(today, today.plusDays(DEFAULT_DAYS));
    }

    String deadlineInstant() {
        return deadline.atStartOfDay(ZoneId.of("America/Fortaleza")).toInstant().toString();
    }

    String startedOnLabel() { return startedOn.format(DATE_FORMAT); }
    String deadlineLabel() { return deadline.atStartOfDay(ZoneId.of("America/Fortaleza")).format(DEADLINE_FORMAT); }
    String deliveryLabel() { return "Até " + deadlineLabel() + " (7 dias após a escolha)"; }
}

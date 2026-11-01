package edu.br.resource.resourcesystem.dto.response;

import edu.br.resource.resourcesystem.model.enums.MatchStatus;

import java.time.*;
import java.time.format.DateTimeFormatter;

public record DonationProposalResponse(
        String protocol,
        String institution,
        int quantity,
        MatchStatus status,
        String statusLabel,
        Instant createdAt) {
    public String dateLabel() {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.of("America/Fortaleza"))
                .format(createdAt);
    }
}


package edu.br.resource.resourcesystem.dto.response;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record DonationHistoryResponse(
        Integer id,
        String itemName,
        String recipientName,
        Instant createdAt,
        String status,
        String statusLabel,
        String actionLabel,
        String actionUrl,
        boolean editable
) {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    .withZone(ZoneId.of("America/Fortaleza"));

    public String dateLabel() {
        return createdAt == null
                ? "—"
                : DATE_FORMAT.format(createdAt);
    }
}

package edu.br.resource.resourcesystem.dto.response;

import edu.br.resource.resourcesystem.model.enums.*;

import java.time.*;
import java.time.format.DateTimeFormatter;

public record DonationHistoryResponse(
        Integer id,
        String itemName,
        String iconPath,
        ItemCategory category,
        String categoryLabel,
        ItemCondition condition,
        String description,
        int quantity,
        long availableQuantity,
        long reservedQuantity,
        long receivedQuantity,
        Instant createdAt,
        DonationStatus status,
        String statusLabel,
        long proposalCount,
        long activeProposals,
        boolean editable,
        boolean deletable,
        String editRestriction,
        String deleteRestriction,
        long version,
        boolean archived) {
    public String dateLabel() {
        return createdAt == null
                ? "—"
                : DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .withZone(ZoneId.of("America/Fortaleza"))
                        .format(createdAt);
    }
}

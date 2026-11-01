package edu.br.resource.resourcesystem.dto.response;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record DonationHistoryResponse(
        Integer id,
        String itemName,
        String iconPath,
        String categoryLabel,
        String condition,
        String description,
        int quantity,
        long availableQuantity,
        Instant createdAt,
        String status,
        String statusLabel,
        String recipientName,
        String proposalProtocol,
        String proposalCaption,
        long otherActiveProposals,
        String actionLabel,
        String actionUrl,
        boolean editable,
        boolean deletable) {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("America/Fortaleza"));

    public String dateLabel() {
        return createdAt == null ? "—" : DATE_FORMAT.format(createdAt);
    }

    public long committedQuantity() {
        return quantity - availableQuantity;
    }

    public boolean hasProposal() {
        return proposalProtocol != null;
    }

    public String quantityLabel() {
        return quantity
                + (quantity == 1 ? " unidade" : " unidades")
                + " · "
                + availableQuantity
                + (availableQuantity == 1 ? " disponível" : " disponíveis");
    }

    public String otherProposalsLabel() {
        return "+" + otherActiveProposals
                + (otherActiveProposals == 1
                        ? " outra proposta ativa"
                        : " outras propostas ativas");
    }
}

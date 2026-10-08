package edu.br.resource.resourcesystem.dto.response;

import edu.br.resource.resourcesystem.model.enums.DonationStage;
import edu.br.resource.resourcesystem.model.enums.MatchStatus;
import java.time.Instant;
import java.util.List;

public record MatchDetailResponse(
        Integer id,
        String protocol,
        MatchStatus status,
        ProfileResponse donor,
        RecipientResponse recipient,
        InventoryDonationResponse donation,
        NeedSummaryResponse need,
        Integer allocatedQuantity,
        String proposalMessage,
        Instant createdAt,
        Instant acceptedAt,
        Instant rejectedAt,
        String rejectionReason,
        Instant cancelledAt,
        String cancellationReason,
        DonationStage expiredStage,
        Instant completedAt,
        DonationDeadlineResponse deadlines,
        DeliveryResponse delivery,
        List<DonationHistoryEventResponse> history,
        MatchActionsResponse actions) {

    public MatchDetailResponse {
        history = List.copyOf(history);
    }
}

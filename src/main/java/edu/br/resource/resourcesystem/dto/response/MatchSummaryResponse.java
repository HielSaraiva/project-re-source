package edu.br.resource.resourcesystem.dto.response;

import edu.br.resource.resourcesystem.model.enums.DeliveryMethod;
import edu.br.resource.resourcesystem.model.enums.DonationStage;
import edu.br.resource.resourcesystem.model.enums.MatchStatus;
import java.time.Instant;

public record MatchSummaryResponse(
        Integer id,
        String protocol,
        String item,
        String icon,
        Integer quantity,
        String donor,
        String organization,
        MatchStatus status,
        Instant createdAt,
        DeliveryMethod deliveryMethod,
        DonationStage deadlineStage,
        Instant deadline) {}

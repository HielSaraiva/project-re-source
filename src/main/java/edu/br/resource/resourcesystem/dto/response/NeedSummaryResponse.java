package edu.br.resource.resourcesystem.dto.response;

import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import edu.br.resource.resourcesystem.model.enums.NecessityPriority;
import edu.br.resource.resourcesystem.model.enums.NecessityStatus;
import java.math.BigDecimal;

public record NeedSummaryResponse(
        Integer id,
        Integer institutionId,
        String organization,
        String item,
        String icon,
        String description,
        ItemCategory category,
        NecessityPriority priority,
        NecessityStatus status,
        Integer requestedQuantity,
        long remainingQuantity,
        LocationResponse location,
        BigDecimal distanceKm) {
}

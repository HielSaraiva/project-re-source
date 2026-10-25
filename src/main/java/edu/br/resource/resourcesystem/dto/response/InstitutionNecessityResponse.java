package edu.br.resource.resourcesystem.dto.response;

import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import edu.br.resource.resourcesystem.model.enums.NecessityPriority;
import edu.br.resource.resourcesystem.model.enums.NecessityStatus;

public record InstitutionNecessityResponse(
        Integer id,
        String itemName,
        String icon,
        ItemCategory category,
        String description,
        Integer quantity,
        long remainingQuantity,
        NecessityPriority priority,
        NecessityStatus status) {}

package edu.br.resource.resourcesystem.dto.response;

import edu.br.resource.resourcesystem.model.enums.DonationStatus;
import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import edu.br.resource.resourcesystem.model.enums.ItemCondition;

public record InventoryDonationResponse(
        Integer id,
        String item,
        String icon,
        ItemCategory category,
        ItemCondition condition,
        String description,
        Integer totalQuantity,
        long availableQuantity,
        DonationStatus status) {}

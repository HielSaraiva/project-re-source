package edu.br.resource.resourcesystem.dto.response;

import edu.br.resource.resourcesystem.model.enums.DeliveryMethod;

import java.util.List;

public record DeliveryOptionResponse(
        DeliveryMethod method,
        String title,
        String description,
        String badge,
        String icon,
        String action,
        List<DonationDetailResponse> details,
        boolean selected) {
    public DeliveryOptionResponse {
        details = List.copyOf(details);
    }
}

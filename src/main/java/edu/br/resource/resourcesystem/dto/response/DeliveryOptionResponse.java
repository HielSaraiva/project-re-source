package edu.br.resource.resourcesystem.dto.response;

import java.util.List;
import edu.br.resource.resourcesystem.model.enums.DeliveryMethod;

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

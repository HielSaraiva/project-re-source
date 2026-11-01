package edu.br.resource.resourcesystem.dto.response;

import java.util.List;

public record ShippingPageResponse(
        MatchDetailResponse donation, List<DeliveryOptionResponse> options) {
    public ShippingPageResponse {
        options = List.copyOf(options);
    }
}

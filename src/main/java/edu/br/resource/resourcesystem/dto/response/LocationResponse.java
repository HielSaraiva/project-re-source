package edu.br.resource.resourcesystem.dto.response;

import java.math.BigDecimal;

public record LocationResponse(
        Integer cityId,
        String city,
        String state,
        BigDecimal latitude,
        BigDecimal longitude) {
}

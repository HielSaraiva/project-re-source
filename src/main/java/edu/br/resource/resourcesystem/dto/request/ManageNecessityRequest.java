package edu.br.resource.resourcesystem.dto.request;

import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import edu.br.resource.resourcesystem.model.enums.NecessityPriority;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ManageNecessityRequest(
        @NotNull ItemCategory category,
        @NotBlank @Size(max = 150) String description,
        @NotNull @Positive @Digits(integer = 10, fraction = 0) @DecimalMax("2147483647")
                BigDecimal quantity,
        @NotNull NecessityPriority priority,
        @PositiveOrZero Long version) {
    public ManageNecessityRequest(
            ItemCategory category,
            String description,
            Integer quantity,
            NecessityPriority priority) {
        this(
                category,
                description,
                quantity == null ? null : BigDecimal.valueOf(quantity),
                priority,
                0L);
    }
}

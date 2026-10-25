package edu.br.resource.resourcesystem.dto.request;

import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import edu.br.resource.resourcesystem.model.enums.NecessityPriority;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ManageNecessityRequest(
        @NotNull ItemCategory category,
        @NotBlank @Size(max = 150) String description,
        @NotNull
                @Positive
                @Digits(integer = 10, fraction = 0)
                @DecimalMax("2147483647")
                BigDecimal quantity,
        @NotNull NecessityPriority priority) {
    public ManageNecessityRequest(
            ItemCategory category,
            String description,
            Integer quantity,
            NecessityPriority priority) {
        this(
                category,
                description,
                quantity == null ? null : BigDecimal.valueOf(quantity),
                priority);
    }
}

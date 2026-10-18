package edu.br.resource.resourcesystem.dto.request;

import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import edu.br.resource.resourcesystem.model.enums.NecessityPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ManageNecessityRequest(
        @NotNull ItemCategory category,
        @NotBlank @Size(max = 150) String description,
        @NotNull @Positive Integer quantity,
        @NotNull NecessityPriority priority) {}

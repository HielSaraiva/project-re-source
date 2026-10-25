package edu.br.resource.resourcesystem.dto.request;

import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import edu.br.resource.resourcesystem.model.enums.NecessityPriority;
import edu.br.resource.resourcesystem.model.enums.NecessityStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record NecessityFilterRequest(
        @Size(max = 200) String query,
        ItemCategory category,
        NecessityPriority priority,
        NecessityStatus status,
        @Min(1) Integer page,
        @Min(1) @Max(100) Integer size) {
    public NecessityFilterRequest {
        page = page == null ? 1 : page;
        size = size == null ? 9 : size;
        status = status == null ? NecessityStatus.ACTIVE : status;
    }
}

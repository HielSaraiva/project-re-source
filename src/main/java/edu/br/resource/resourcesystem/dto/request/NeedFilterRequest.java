package edu.br.resource.resourcesystem.dto.request;

import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import edu.br.resource.resourcesystem.model.enums.NecessityPriority;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record NeedFilterRequest(
        @Size(max = 200) String query,
        ItemCategory category,
        NecessityPriority priority,
        @Positive Integer stateId,
        @Min(1) Integer page,
        @Min(1) @Max(100) Integer size) {
    public NeedFilterRequest {
        page = page == null ? 1 : page;
        size = size == null ? 6 : size;
    }
}

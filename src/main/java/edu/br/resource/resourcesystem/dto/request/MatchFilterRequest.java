package edu.br.resource.resourcesystem.dto.request;

import edu.br.resource.resourcesystem.dto.MatchSortOrder;
import edu.br.resource.resourcesystem.model.enums.MatchStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record MatchFilterRequest(
        @Size(max = 200) String query,
        MatchStatus status,
        MatchSortOrder order,
        @Min(1) Integer page,
        @Min(1) @Max(100) Integer size) {
    public MatchFilterRequest {
        page = page == null ? 1 : page;
        size = size == null ? 6 : size;
        order = order == null ? MatchSortOrder.RECENT : order;
    }
}

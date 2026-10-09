package edu.br.resource.resourcesystem.dto.response;

import java.util.List;

public record DonorDashboardResponse(
        ProfileResponse profile,
        List<NeedSummaryResponse> urgentNeeds,
        List<MatchSummaryResponse> donations) {

    public DonorDashboardResponse {
        urgentNeeds = List.copyOf(urgentNeeds);
        donations = List.copyOf(donations);
    }
}

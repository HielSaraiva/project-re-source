package edu.br.resource.resourcesystem.dto.response;

import java.util.List;

public record InstitutionDashboardResponse(
        ProfileResponse profile,
        long receivedDonations,
        long pendingAcceptances,
        long activeNeeds,
        List<MatchSummaryResponse> donations,
        List<NeedSummaryResponse> needs) {

    public InstitutionDashboardResponse {
        donations = List.copyOf(donations);
        needs = List.copyOf(needs);
    }
}

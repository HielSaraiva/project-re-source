package edu.br.resource.resourcesystem.dto.response;

import java.util.List;

public record ProposalPageResponse(
        NeedSummaryResponse need, List<InventoryDonationResponse> availableDonations) {

    public ProposalPageResponse {
        availableDonations = List.copyOf(availableDonations);
    }
}

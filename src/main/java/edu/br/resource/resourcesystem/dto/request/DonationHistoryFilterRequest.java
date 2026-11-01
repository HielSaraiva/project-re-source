package edu.br.resource.resourcesystem.dto.request;

import jakarta.validation.constraints.Min;

public record DonationHistoryFilterRequest(@Min(1) Integer page) {
    public static final int PAGE_SIZE = 10;

    public DonationHistoryFilterRequest {
        page = page == null ? 1 : page;
    }
}

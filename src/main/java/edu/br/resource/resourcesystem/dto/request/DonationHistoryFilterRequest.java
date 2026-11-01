package edu.br.resource.resourcesystem.dto.request;

import edu.br.resource.resourcesystem.model.enums.DonationStatus;
import edu.br.resource.resourcesystem.model.enums.ItemCategory;

import jakarta.validation.constraints.*;

public record DonationHistoryFilterRequest(
        @Size(max = 200) String query,
        ItemCategory category,
        DonationStatus status,
        @Min(1) Integer page,
        @Pattern(regexp = "inventory|completed|archived") String view) {
    public static final int PAGE_SIZE = 9;

    public DonationHistoryFilterRequest {
        page = page == null ? 1 : page;
        view = view == null ? "inventory" : view;
    }

    public static java.util.List<DonationStatus> selectableStatuses() {
        return java.util.List.of(
                DonationStatus.REGISTERED,
                DonationStatus.AWAITING_ACCEPTANCE,
                DonationStatus.AWAITING_SHIPMENT,
                DonationStatus.AWAITING_DELIVERY,
                DonationStatus.IN_TRANSIT,
                DonationStatus.AWAITING_NGO_CONFIRMATION);
    }

    public boolean archived() {
        return "archived".equals(view);
    }

    public boolean completed() {
        return "completed".equals(view);
    }

    public boolean historical() {
        return archived() || completed();
    }

    @AssertTrue(message = "Selecione um status disponível no filtro de doações.")
    public boolean isSupportedStatus() {
        return status == null || selectableStatuses().contains(status);
    }

    public DonationHistoryFilterRequest(
            String query, ItemCategory category, DonationStatus status, Integer page) {
        this(query, category, status, page, "inventory");
    }
}

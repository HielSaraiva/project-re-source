package edu.br.resource.resourcesystem.view;

import edu.br.resource.resourcesystem.controller.donor.DonorDonationProposalController;
import edu.br.resource.resourcesystem.service.donor.DonorDashboardService;
import edu.br.resource.resourcesystem.service.donor.DonorNeedsService;
import edu.br.resource.resourcesystem.service.donor.DonorInventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DonorDonationProposalTest {

    static Stream<String> linkedNeeds() {
        var listedIds = new DonorNeedsService().needs().stream().map(DonorNeedsService.Need::id);
        var urgencies = (List<?>) new DonorDashboardService().dashboardData().get("urgencies");
        var urgentIds = urgencies.stream().map(urgency -> ((DonorDashboardService.Urgency) urgency).needId());
        return Stream.concat(listedIds, urgentIds);
    }

    @ParameterizedTest
    @MethodSource("linkedNeeds")
    void opensProposalForEachHelpNowLink(String needId) {
        var needsService = new DonorNeedsService();
        var model = new ExtendedModelMap();
        var template = new DonorDonationProposalController(needsService, new DonorInventoryService()).proposal(needId, model);

        assertThat(template).isEqualTo("donor/donation-proposal");
        assertThat(model.get("need")).isEqualTo(needsService.findById(needId).orElseThrow());
        assertThat(model.get("donorName")).isEqualTo("João Silva");
        var need = needsService.findById(needId).orElseThrow();
        assertThat(model.get("inventoryDonations"))
                .isEqualTo(new DonorInventoryService().registeredDonationsForCategory(need.category()));
    }

    @Test
    void unknownNeedDoesNotFallBackToAnUnrelatedOrganization() {
        var controller = new DonorDonationProposalController(new DonorNeedsService(), new DonorInventoryService());
        assertThatThrownBy(() -> controller.proposal("unknown", new ExtendedModelMap()))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}

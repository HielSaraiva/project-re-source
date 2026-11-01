package edu.br.resource.resourcesystem.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.br.resource.resourcesystem.model.entity.DonationMatch;
import edu.br.resource.resourcesystem.model.enums.DonationStatus;
import edu.br.resource.resourcesystem.model.enums.MatchStatus;

import org.junit.jupiter.api.Test;

import java.util.List;

class DonationManagementRulesTest {
    private static DonationMatch match(MatchStatus status) {
        return DonationMatch.builder().status(status).allocatedQuantity(1).build();
    }

    @Test
    void editingIsBlockedOnlyWhileThereAreActiveProposals() {
        assertTrue(DonationManagementService.canEdit(List.of()));
        assertTrue(DonationManagementService.canEdit(List.of(match(MatchStatus.COMPLETED))));
        assertTrue(DonationManagementService.canEdit(List.of(match(MatchStatus.REJECTED))));
        assertFalse(DonationManagementService.canEdit(List.of(match(MatchStatus.PROPOSED))));
        assertFalse(
                DonationManagementService.canEdit(
                        List.of(match(MatchStatus.COMPLETED), match(MatchStatus.IN_TRANSIT))));
    }

    @Test
    void aCompletedDonationCanNotBeEdited() {
        assertFalse(DonationManagementService.canEdit(DonationStatus.COMPLETED, List.of()));
        assertFalse(
                DonationManagementService.canEdit(
                        DonationStatus.COMPLETED, List.of(match(MatchStatus.COMPLETED))));
        assertTrue(
                DonationManagementService.canEdit(
                        DonationStatus.REGISTERED, List.of(match(MatchStatus.COMPLETED))));
        assertFalse(
                DonationManagementService.canEdit(
                        DonationStatus.AWAITING_ACCEPTANCE, List.of(match(MatchStatus.PROPOSED))));
    }

    @Test
    void deletingIsAllowedOnlyWithoutAnyProposal() {
        assertTrue(DonationManagementService.canDelete(List.of()));
        for (var status : MatchStatus.values())
            assertFalse(DonationManagementService.canDelete(List.of(match(status))), status.name());
    }
}

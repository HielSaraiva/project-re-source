package edu.br.resource.resourcesystem.quality.unit.match;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.messaging.MatchEventOutbox;
import edu.br.resource.resourcesystem.model.entity.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.repository.*;
import edu.br.resource.resourcesystem.service.DonationManagementService;
import edu.br.resource.resourcesystem.service.match.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RN-002/RN-004/RN-005 — rejeições do serviço sem dependências externas")
class MatchWorkflowServiceTest {
    @Mock DonationManagementService inventory;
    @Mock MatchLockPolicy locks;
    @Mock DonationRepository donations;
    @Mock NecessityRepository necessities;
    @Mock DonationMatchRepository matches;
    @Mock DeliveryRepository deliveries;
    @Mock MatchStatusHistoryRepository history;
    @Mock DonationStatusHistoryRepository donationHistory;
    @Mock MatchDtoMapper mapper;
    @Mock MatchEventOutbox outbox;
    @Mock MatchProtocolRepository protocols;
    @Mock jakarta.persistence.EntityManager entityManager;
    @InjectMocks MatchWorkflowService sut;

    Donation donation() {
        return Donation.builder()
                .id(1)
                .donor(User.builder().id(1).build())
                .donationPackage(
                        DonationPackage.builder()
                                .quantity(10)
                                .item(
                                        Item.builder()
                                                .title("Item fictício")
                                                .condition(ItemCondition.GOOD)
                                                .itemType(
                                                        ItemType.builder()
                                                                .category(ItemCategory.ELECTRONICS)
                                                                .build())
                                                .build())
                                .build())
                .build();
    }

    Necessity need() {
        return Necessity.builder()
                .id(2)
                .quantityRequested(10)
                .institution(Institution.builder().id(3).status(InstitutionStatus.APPROVED).build())
                .itemType(ItemType.builder().category(ItemCategory.ELECTRONICS).build())
                .build();
    }

    void available(Donation d, Necessity n) {
        when(donations.findForUpdate(1, 1)).thenReturn(Optional.of(d));
        when(necessities.findForUpdate(2)).thenReturn(Optional.of(n));
    }

    @Test
    void propose_WithMissingOwnedDonation_DoesNotQueryNeedOrWrite() {
        assertThatThrownBy(() -> sut.propose(1, new SubmitProposalRequest(2, 1, 1, null)))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(necessities, outbox, protocols);
    }

    @Test
    void propose_WithMissingNeed_DoesNotAllocate() {
        when(donations.findForUpdate(1, 1)).thenReturn(Optional.of(donation()));
        assertThatThrownBy(() -> sut.propose(1, new SubmitProposalRequest(2, 1, 1, null)))
                .isInstanceOf(ResponseStatusException.class);
        verify(matches, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @EnumSource(
            value = NecessityStatus.class,
            names = {"FULFILLED", "CANCELLED"})
    void propose_WithClosedNeed_DoesNotWrite(NecessityStatus status) {
        var n = need();
        n.setStatus(status);
        available(donation(), n);
        assertThatThrownBy(() -> sut.propose(1, new SubmitProposalRequest(2, 1, 1, null)))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(matches, outbox);
    }

    @Test
    void propose_WithCategoryMismatch_DoesNotReserve() {
        var d = donation();
        d.getDonationPackage().getItem().getItemType().setCategory(ItemCategory.FOOD);
        available(d, need());
        assertThatThrownBy(() -> sut.propose(1, new SubmitProposalRequest(2, 1, 1, null)))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(matches, outbox);
    }

    @Test
    void propose_WithInsufficientStock_DoesNotGenerateProtocol() {
        available(donation(), need());
        when(matches.sumAllocatedByDonation(eq(1), any())).thenReturn(9L);
        assertThatThrownBy(() -> sut.propose(1, new SubmitProposalRequest(2, 1, 2, null)))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(protocols, outbox);
    }

    @Test
    void propose_WithInsufficientNeed_DoesNotGenerateProtocol() {
        available(donation(), need());
        when(matches.sumAllocatedByNecessity(eq(2), any())).thenReturn(9L);
        assertThatThrownBy(() -> sut.propose(1, new SubmitProposalRequest(2, 1, 2, null)))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(protocols, outbox);
    }

    @Test
    void propose_WithActiveDuplicate_DoesNotCreateSecondMatch() {
        available(donation(), need());
        when(matches.existsByDonationIdAndNecessityIdAndStatusIn(eq(1), eq(2), any()))
                .thenReturn(true);
        assertThatThrownBy(() -> sut.propose(1, new SubmitProposalRequest(2, 1, 1, null)))
                .isInstanceOf(ResponseStatusException.class);
        verify(matches, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "x"})
    void reject_WithMissingReasonOrUnknownProtocol_DoesNotWrite(String reason) {
        assertThatThrownBy(() -> sut.reject(3, "INT-2026-1", reason))
                .isInstanceOf(ResponseStatusException.class);
        verify(matches, never()).saveAndFlush(any());
    }

    @Test
    void reject_WithTooLongReason_RejectsBeforeReading() {
        assertThatThrownBy(() -> sut.reject(3, "INT-2026-1", "x".repeat(1001)))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(matches);
    }

    @ParameterizedTest
    @EnumSource(
            value = DeliveryMethod.class,
            names = {"RESOURCE_LOGISTICS"})
    void select_WithUnsupportedMethod_RejectsBeforeReading(DeliveryMethod method) {
        assertThatThrownBy(() -> sut.selectMethod(1, "INT-2026-1", method))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(matches);
    }

    @Test
    void register_WithMissingNeed_DoesNotCreateInventory() {
        assertThatThrownBy(
                        () ->
                                sut.register(
                                        1,
                                        new RegisterDonationRequest(
                                                2, "Item", 1, ItemCondition.GOOD, null)))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(inventory);
    }
}

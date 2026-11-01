package edu.br.resource.resourcesystem.service.necessity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.model.entity.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.presentation.CategoryIconResolver;
import edu.br.resource.resourcesystem.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class NecessityManagementServiceTest {
    @Mock NecessityRepository necessities;
    @Mock ItemTypeRepository itemTypes;
    @Mock InstitutionRepository institutions;
    @Mock DonationMatchRepository matches;
    @Mock CategoryIconResolver icons;
    @Mock MatchLockPolicy locks;
    NecessityManagementService service;
    Necessity need;

    @BeforeEach
    void setup() {
        service =
                new NecessityManagementService(
                        necessities, itemTypes, institutions, matches, icons, locks);
        need =
                Necessity.builder()
                        .id(12)
                        .institution(Institution.builder().id(7).build())
                        .itemType(
                                ItemType.builder()
                                        .id(3)
                                        .category(ItemCategory.FURNITURE)
                                        .name("Cadeiras")
                                        .build())
                        .quantityRequested(10)
                        .build();
    }

    private ManageNecessityRequest request() {
        return new ManageNecessityRequest(
                ItemCategory.FURNITURE, "Cadeiras", 15, NecessityPriority.HIGH);
    }

    private void locked() {
        when(necessities.findForUpdate(12)).thenReturn(Optional.of(need));
    }

    @Test
    void updateRejectsAnotherOwner() {
        locked();
        assertEquals(
                404,
                assertThrows(ResponseStatusException.class, () -> service.update(8, 12, request()))
                        .getStatusCode()
                        .value());
        verifyNoInteractions(itemTypes);
    }

    @Test
    void blockedProposalRejectsTheWholeEdit() {
        locked();
        when(matches.existsByNecessityIdAndStatusNotIn(
                        12, Set.of(MatchStatus.CANCELLED, MatchStatus.REJECTED)))
                .thenReturn(true);
        assertEquals(
                409,
                assertThrows(ResponseStatusException.class, () -> service.update(7, 12, request()))
                        .getStatusCode()
                        .value());
        verifyNoInteractions(itemTypes);
        assertEquals(10, need.getQuantityRequested());
    }

    @Test
    void staleVersionDoesNotOverwriteData() {
        locked();
        need.setVersion(1);
        assertEquals(
                409,
                assertThrows(ResponseStatusException.class, () -> service.update(7, 12, request()))
                        .getStatusCode()
                        .value());
        verifyNoInteractions(itemTypes);
    }

    @Test
    void completedNeedCannotBeEdited() {
        locked();
        need.setStatus(NecessityStatus.FULFILLED);
        assertEquals(
                409,
                assertThrows(ResponseStatusException.class, () -> service.update(7, 12, request()))
                        .getStatusCode()
                        .value());
    }

    @Test
    void cancelledHistoryIsRetainedWhenDeleting() {
        locked();
        when(matches.existsByNecessityId(12)).thenReturn(true);
        service.delete(7, 12, 0);
        assertNotNull(need.getDeletedAt());
        assertEquals(NecessityStatus.CANCELLED, need.getStatus());
        verify(necessities, never()).delete(any(Necessity.class));
        verify(locks).apply();
    }

    @Test
    void deletionWithoutHistoryRemovesTheRow() {
        locked();
        service.delete(7, 12, 0);
        verify(necessities).delete(need);
    }

    @Test
    void deletionWithOtherProposalsIsRejected() {
        locked();
        when(matches.existsByNecessityIdAndStatusNotIn(
                        12, Set.of(MatchStatus.CANCELLED, MatchStatus.REJECTED)))
                .thenReturn(true);
        assertEquals(
                409,
                assertThrows(ResponseStatusException.class, () -> service.delete(7, 12, 0))
                        .getStatusCode()
                        .value());
        verify(necessities, never()).delete(any(Necessity.class));
        assertNull(need.getDeletedAt());
    }

    @Test
    void removedNeedIsNotExposed() {
        need.setDeletedAt(java.time.Instant.now());
        when(necessities.findById(12)).thenReturn(Optional.of(need));
        assertEquals(
                404,
                assertThrows(ResponseStatusException.class, () -> service.editDetails(7, 12))
                        .getStatusCode()
                        .value());
    }
}

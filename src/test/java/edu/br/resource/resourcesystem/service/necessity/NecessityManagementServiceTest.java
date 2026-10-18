package edu.br.resource.resourcesystem.service.necessity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import edu.br.resource.resourcesystem.dto.request.ManageNecessityRequest;
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

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class NecessityManagementServiceTest {
    @Mock private NecessityRepository necessities;
    @Mock private ItemTypeRepository itemTypes;
    @Mock private InstitutionRepository institutions;
    @Mock private DonationMatchRepository matches;
    @Mock private CategoryIconResolver icons;

    private NecessityManagementService service;

    @BeforeEach
    void setUp() {
        service =
                new NecessityManagementService(
                        necessities, itemTypes, institutions, matches, icons);
    }

    @Test
    void createAssociatesTheNeedWithTheAuthenticatedInstitution() {
        var institution = Institution.builder().id(7).build();
        var itemType =
                ItemType.builder()
                        .id(11)
                        .category(ItemCategory.FURNITURE)
                        .name("Cadeiras escolares")
                        .build();
        when(institutions.findById(7)).thenReturn(Optional.of(institution));
        when(itemTypes.findByCategoryAndNameIgnoreCase(
                        ItemCategory.FURNITURE, "Cadeiras escolares"))
                .thenReturn(Optional.of(itemType));

        service.create(
                7,
                new ManageNecessityRequest(
                        ItemCategory.FURNITURE,
                        " Cadeiras escolares ",
                        50,
                        NecessityPriority.HIGH));

        var saved = org.mockito.ArgumentCaptor.forClass(Necessity.class);
        verify(necessities).save(saved.capture());
        assertEquals(institution, saved.getValue().getInstitution());
        assertEquals(itemType, saved.getValue().getItemType());
        assertEquals("Cadeiras escolares", saved.getValue().getDescription());
        assertEquals(50, saved.getValue().getQuantityRequested());
        assertEquals(NecessityPriority.HIGH, saved.getValue().getPriority());
    }

    @Test
    void createStoresAnItemTypeWhenItDoesNotExistYet() {
        var institution = Institution.builder().id(7).build();
        var itemType =
                ItemType.builder()
                        .id(11)
                        .category(ItemCategory.FURNITURE)
                        .name("Cadeiras escolares")
                        .build();
        when(institutions.findById(7)).thenReturn(Optional.of(institution));
        when(itemTypes.findByCategoryAndNameIgnoreCase(
                        ItemCategory.FURNITURE, "Cadeiras escolares"))
                .thenReturn(Optional.empty());
        when(itemTypes.save(any(ItemType.class))).thenReturn(itemType);

        service.create(
                7,
                new ManageNecessityRequest(
                        ItemCategory.FURNITURE,
                        "Cadeiras escolares",
                        50,
                        NecessityPriority.MEDIUM));

        var saved = org.mockito.ArgumentCaptor.forClass(Necessity.class);
        verify(necessities).save(saved.capture());
        assertEquals(itemType, saved.getValue().getItemType());
        assertEquals("Cadeiras escolares", saved.getValue().getDescription());
        verify(itemTypes).save(any(ItemType.class));
    }

    @Test
    void updateRejectsNeedsOwnedByAnotherInstitution() {
        var necessity =
                Necessity.builder()
                        .id(12)
                        .institution(Institution.builder().id(8).build())
                        .status(NecessityStatus.ACTIVE)
                        .build();
        when(necessities.findForUpdate(12)).thenReturn(Optional.of(necessity));

        var error =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                service.update(
                                        7,
                                        12,
                                        new ManageNecessityRequest(
                                                ItemCategory.FURNITURE,
                                                "Cadeiras",
                                                5,
                                                NecessityPriority.LOW)));

        assertEquals(404, error.getStatusCode().value());
        verifyNoInteractions(matches, itemTypes);
    }

    @Test
    void updateRejectsQuantityBelowExistingAllocations() {
        var necessity =
                Necessity.builder()
                        .id(12)
                        .institution(Institution.builder().id(7).build())
                        .itemType(
                                ItemType.builder()
                                        .category(ItemCategory.FURNITURE)
                                        .name("Cadeiras")
                                        .build())
                        .status(NecessityStatus.ACTIVE)
                        .quantityRequested(20)
                        .priority(NecessityPriority.MEDIUM)
                        .build();
        when(necessities.findForUpdate(12)).thenReturn(Optional.of(necessity));
        when(matches.sumAllocatedByNecessity(
                        12, MatchStatus.allocationConsumingStatuses()))
                .thenReturn(10L);

        var error =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                service.update(
                                        7,
                                        12,
                                        new ManageNecessityRequest(
                                                ItemCategory.FURNITURE,
                                                "Cadeiras",
                                                9,
                                                NecessityPriority.HIGH)));

        assertEquals(409, error.getStatusCode().value());
        assertEquals(20, necessity.getQuantityRequested());
        assertEquals(NecessityPriority.MEDIUM, necessity.getPriority());
        verifyNoInteractions(itemTypes);
    }

    @Test
    void updateRejectsChangingCategoryWhenProposalsAreAllocated() {
        var necessity =
                Necessity.builder()
                        .id(12)
                        .institution(Institution.builder().id(7).build())
                        .itemType(
                                ItemType.builder()
                                        .category(ItemCategory.FURNITURE)
                                        .name("Cadeiras")
                                        .build())
                        .status(NecessityStatus.ACTIVE)
                        .quantityRequested(20)
                        .build();
        when(necessities.findForUpdate(12)).thenReturn(Optional.of(necessity));
        when(matches.sumAllocatedByNecessity(
                        12, MatchStatus.allocationConsumingStatuses()))
                .thenReturn(2L);

        var error =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                service.update(
                                        7,
                                        12,
                                        new ManageNecessityRequest(
                                                ItemCategory.EDUCATION,
                                                "Cadernos",
                                                20,
                                                NecessityPriority.HIGH)));

        assertEquals(409, error.getStatusCode().value());
        assertEquals(ItemCategory.FURNITURE, necessity.getItemType().getCategory());
        verifyNoInteractions(itemTypes);
    }

    @Test
    void updateReusesOrCreatesTheNewItemTypeWithoutChangingTheExistingOne() {
        var oldType =
                ItemType.builder()
                        .id(3)
                        .category(ItemCategory.FURNITURE)
                        .name("Cadeira")
                        .build();
        var necessity =
                Necessity.builder()
                        .id(12)
                        .institution(Institution.builder().id(7).build())
                        .itemType(oldType)
                        .status(NecessityStatus.ACTIVE)
                        .quantityRequested(20)
                        .build();
        var replacement =
                ItemType.builder()
                        .id(4)
                        .category(ItemCategory.FURNITURE)
                        .name("Cadeiras infantis")
                        .build();
        when(necessities.findForUpdate(12)).thenReturn(Optional.of(necessity));
        when(matches.sumAllocatedByNecessity(
                        12, MatchStatus.allocationConsumingStatuses()))
                .thenReturn(5L);
        when(itemTypes.findByCategoryAndNameIgnoreCase(
                        ItemCategory.FURNITURE, "Cadeiras infantis"))
                .thenReturn(Optional.of(replacement));

        service.update(
                7,
                12,
                new ManageNecessityRequest(
                        ItemCategory.FURNITURE,
                        "Cadeiras infantis",
                        12,
                        NecessityPriority.LOW));

        assertEquals("Cadeira", oldType.getName());
        assertEquals(replacement, necessity.getItemType());
        assertEquals("Cadeiras infantis", necessity.getDescription());
        assertEquals(12, necessity.getQuantityRequested());
        assertEquals(NecessityPriority.LOW, necessity.getPriority());
        verify(necessities, never()).save(any());
    }

    @Test
    void updateRejectsInactiveNeeds() {
        var necessity =
                Necessity.builder()
                        .id(12)
                        .institution(Institution.builder().id(7).build())
                        .status(NecessityStatus.FULFILLED)
                        .build();
        when(necessities.findForUpdate(12)).thenReturn(Optional.of(necessity));

        var error =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                service.update(
                                        7,
                                        12,
                                        new ManageNecessityRequest(
                                                ItemCategory.FURNITURE,
                                                "Cadeiras",
                                                5,
                                                NecessityPriority.LOW)));

        assertEquals(409, error.getStatusCode().value());
        verifyNoInteractions(matches, itemTypes);
    }
}

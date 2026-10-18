package edu.br.resource.resourcesystem.service.necessity;

import edu.br.resource.resourcesystem.dto.request.ManageNecessityRequest;
import edu.br.resource.resourcesystem.dto.response.InstitutionNecessityResponse;
import edu.br.resource.resourcesystem.model.entity.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.presentation.CategoryIconResolver;
import edu.br.resource.resourcesystem.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class NecessityManagementService {
    private final NecessityRepository necessities;
    private final ItemTypeRepository itemTypes;
    private final InstitutionRepository institutions;
    private final DonationMatchRepository matches;
    private final CategoryIconResolver icons;

    public NecessityManagementService(
            NecessityRepository necessities,
            ItemTypeRepository itemTypes,
            InstitutionRepository institutions,
            DonationMatchRepository matches,
            CategoryIconResolver icons) {
        this.necessities = necessities;
        this.itemTypes = itemTypes;
        this.institutions = institutions;
        this.matches = matches;
        this.icons = icons;
    }

    public List<InstitutionNecessityResponse> list(Integer institutionId) {
        return necessities
                .findAllByInstitutionIdAndStatusNotOrderByCreatedAtDesc(
                        institutionId, NecessityStatus.FULFILLED)
                .stream()
                .map(
                        necessity ->
                                new InstitutionNecessityResponse(
                                        necessity.getId(),
                                        necessity.getItemType().getName(),
                                        icons.resolve(necessity.getItemType().getCategory()),
                                        necessity.getItemType().getCategory(),
                                        necessity.getDescription(),
                                        necessity.getQuantityRequested(),
                                        necessity.getPriority(),
                                        necessity.getStatus()))
                .toList();
    }

    @Transactional
    public void create(Integer institutionId, ManageNecessityRequest request) {
        var institution = institutions.findById(institutionId).orElseThrow(this::missing);
        necessities.save(
                Necessity.builder()
                        .institution(institution)
                        .itemType(itemType(request))
                        .description(request.description().strip())
                        .quantityRequested(request.quantity())
                        .priority(request.priority())
                        .build());
    }

    @Transactional
    public void update(
            Integer institutionId, Integer necessityId, ManageNecessityRequest request) {
        var necessity = necessities.findForUpdate(necessityId).orElseThrow(this::missing);
        if (!necessity.getInstitution().getId().equals(institutionId)) throw missing();
        if (necessity.getStatus() != NecessityStatus.ACTIVE)
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Somente necessidades ativas podem ser editadas.");

        long allocated =
                matches.sumAllocatedByNecessity(
                        necessityId, MatchStatus.allocationConsumingStatuses());
        if (allocated > 0
                && request.category() != necessity.getItemType().getCategory())
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A categoria não pode ser alterada enquanto houver propostas associadas.");
        if (request.quantity() < allocated)
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A quantidade mínima é "
                            + allocated
                            + (allocated == 1 ? " unidade" : " unidades")
                            + " para preservar as propostas existentes.");

        necessity.setItemType(itemType(request));
        necessity.setDescription(request.description().strip());
        necessity.setQuantityRequested(request.quantity());
        necessity.setPriority(request.priority());
    }

    @Transactional
    public void delete(Integer institutionId, Integer necessityId) {
        var necessity = necessities.findForUpdate(necessityId).orElseThrow(this::missing);
        if (!necessity.getInstitution().getId().equals(institutionId)) throw missing();
        if (matches.existsByNecessityId(necessityId))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta necessidade não pode ser excluída porque possui propostas associadas.");

        necessities.delete(necessity);
    }

    private ItemType itemType(ManageNecessityRequest request) {
        String name = request.description().strip();
        return itemTypes
                .findByCategoryAndNameIgnoreCase(request.category(), name)
                .orElseGet(
                        () ->
                                itemTypes.save(
                                        ItemType.builder()
                                                .category(request.category())
                                                .name(name)
                                                .build()));
    }

    private ResponseStatusException missing() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Necessidade não encontrada.");
    }
}

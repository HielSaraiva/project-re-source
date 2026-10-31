package edu.br.resource.resourcesystem.service.necessity;

import edu.br.resource.resourcesystem.dto.request.ManageNecessityRequest;
import edu.br.resource.resourcesystem.dto.request.NecessityFilterRequest;
import edu.br.resource.resourcesystem.dto.response.InstitutionNecessityResponse;
import edu.br.resource.resourcesystem.model.entity.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.presentation.CategoryIconResolver;
import edu.br.resource.resourcesystem.repository.*;
import edu.br.resource.resourcesystem.repository.specification.NecessitySpecifications;

import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

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

    public Page<InstitutionNecessityResponse> list(
            Integer institutionId, NecessityFilterRequest filter) {
        var specification =
                NecessitySpecifications.forInstitution(institutionId)
                        .and(NecessitySpecifications.withCategory(filter.category()))
                        .and(NecessitySpecifications.withPriority(filter.priority()))
                        .and(NecessitySpecifications.withStatus(filter.status()))
                        .and(NecessitySpecifications.search(filter.query()));
        var sort =
                Sort.by("createdAt")
                        .descending()
                        .and(Sort.by("id").descending());
        var page =
                necessities.findAll(
                        specification,
                        PageRequest.of(filter.page() - 1, filter.size(), sort));
        if (page.isEmpty() && filter.page() > 1 && page.getTotalPages() > 0)
            page =
                    necessities.findAll(
                            specification,
                            PageRequest.of(page.getTotalPages() - 1, filter.size(), sort));
        return page.map(this::response);
    }

    public InstitutionNecessityResponse editDetails(Integer institutionId, Integer necessityId) {
        var necessity = necessities.findById(necessityId).orElseThrow(this::missing);
        if (!necessity.getInstitution().getId().equals(institutionId)) throw missing();
        return response(necessity);
    }

    private InstitutionNecessityResponse response(Necessity necessity) {
        long received =
                matches.sumAllocatedByNecessity(
                        necessity.getId(), Set.of(MatchStatus.COMPLETED));
        long reserved =
                matches.sumAllocatedByNecessity(
                        necessity.getId(), MatchStatus.activeStatuses());
        long minimumQuantity = received + reserved;
        return new InstitutionNecessityResponse(
                necessity.getId(),
                necessity.getItemType().getName(),
                icons.resolve(necessity.getItemType().getCategory()),
                necessity.getItemType().getCategory(),
                necessity.getDescription(),
                necessity.getQuantityRequested(),
                Math.max(0, necessity.getQuantityRequested() - minimumQuantity),
                reserved,
                received,
                minimumQuantity,
                matches.existsByNecessityId(necessity.getId()),
                necessity.getPriority(),
                necessity.getStatus());
    }

    @Transactional
    public void create(Integer institutionId, ManageNecessityRequest request) {
        var institution = institutions.findById(institutionId).orElseThrow(this::missing);
        necessities.save(
                Necessity.builder()
                        .institution(institution)
                        .itemType(itemType(request))
                        .description(request.description().strip())
                        .quantityRequested(request.quantity().intValueExact())
                        .priority(request.priority())
                        .build());
    }

    @Transactional
    public InstitutionNecessityResponse update(
            Integer institutionId, Integer necessityId, ManageNecessityRequest request) {
        var necessity = necessities.findForUpdate(necessityId).orElseThrow(this::missing);
        if (!necessity.getInstitution().getId().equals(institutionId)) throw missing();
        if (necessity.getStatus() != NecessityStatus.ACTIVE)
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Somente necessidades ativas podem ser editadas.");

        boolean hasProposals = matches.existsByNecessityId(necessityId);
        if (hasProposals
                && request.category() != necessity.getItemType().getCategory())
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A categoria não pode ser alterada enquanto houver propostas associadas.");
        long allocated =
                matches.sumAllocatedByNecessity(
                        necessityId, MatchStatus.allocationConsumingStatuses());
        int requestedQuantity = request.quantity().intValueExact();
        if (requestedQuantity < allocated) {
            long received =
                    matches.sumAllocatedByNecessity(
                            necessityId, Set.of(MatchStatus.COMPLETED));
            long reserved = allocated - received;
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A quantidade mínima é "
                            + allocated
                            + (allocated == 1 ? " unidade: " : " unidades: ")
                            + received
                            + (received == 1 ? " unidade recebida e " : " unidades recebidas e ")
                            + reserved
                            + (reserved == 1 ? " unidade reservada." : " unidades reservadas."));
        }
        long received =
                matches.sumAllocatedByNecessity(
                        necessityId, Set.of(MatchStatus.COMPLETED));
        if (requestedQuantity < necessity.getQuantityRequested()
                && received >= requestedQuantity
                && !Boolean.TRUE.equals(request.confirmFulfillment()))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A quantidade informada marcará a necessidade como atendida. Confirme essa"
                            + " alteração antes de salvar.");

        necessity.setItemType(itemType(request));
        necessity.setDescription(request.description().strip());
        necessity.setQuantityRequested(requestedQuantity);
        necessity.setPriority(request.priority());
        if (received >= requestedQuantity) necessity.setStatus(NecessityStatus.FULFILLED);
        return response(necessity);
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

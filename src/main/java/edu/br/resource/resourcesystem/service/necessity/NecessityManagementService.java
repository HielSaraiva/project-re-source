package edu.br.resource.resourcesystem.service.necessity;

import edu.br.resource.resourcesystem.dto.request.ManageNecessityRequest;
import edu.br.resource.resourcesystem.dto.request.NecessityFilterRequest;
import edu.br.resource.resourcesystem.dto.response.InstitutionNecessityResponse;
import edu.br.resource.resourcesystem.model.entity.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.presentation.CategoryIconResolver;
import edu.br.resource.resourcesystem.repository.*;
import edu.br.resource.resourcesystem.repository.specification.NecessitySpecifications;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NecessityManagementService {
    private final NecessityRepository necessities;
    private final ItemTypeRepository itemTypes;
    private final InstitutionRepository institutions;
    private final DonationMatchRepository matches;
    private final CategoryIconResolver icons;
    private final MatchLockPolicy lockPolicy;

    public Page<InstitutionNecessityResponse> list(
            Integer institutionId, NecessityFilterRequest filter) {
        var specification =
                NecessitySpecifications.notDeleted()
                        .and(NecessitySpecifications.forInstitution(institutionId))
                        .and(NecessitySpecifications.withCategory(filter.category()))
                        .and(NecessitySpecifications.withPriority(filter.priority()))
                        .and(NecessitySpecifications.withStatus(filter.status()))
                        .and(NecessitySpecifications.search(filter.query()));
        var sort = Sort.by("createdAt").descending().and(Sort.by("id").descending());
        var page =
                necessities.findAll(
                        specification, PageRequest.of(filter.page() - 1, filter.size(), sort));
        if (page.isEmpty() && filter.page() > 1 && page.getTotalPages() > 0)
            page =
                    necessities.findAll(
                            specification,
                            PageRequest.of(page.getTotalPages() - 1, filter.size(), sort));
        // Fetch aggregates once per page instead of three queries for every card.
        var totals = totals(page.getContent().stream().map(Necessity::getId).toList());
        return page.map(n -> response(n, totals.get(n.getId())));
    }

    public InstitutionNecessityResponse editDetails(Integer institutionId, Integer necessityId) {
        var necessity = necessities.findById(necessityId).orElseThrow(this::missing);
        requireOwner(necessity, institutionId);
        return response(necessity);
    }

    private Map<Integer, DonationMatchRepository.NecessityTotals> totals(List<Integer> ids) {
        if (ids.isEmpty()) return Map.of();
        return matches
                .summarizeNecessities(
                        ids, MatchStatus.COMPLETED, MatchStatus.REJECTED, MatchStatus.CANCELLED)
                .stream()
                .collect(
                        Collectors.toMap(
                                DonationMatchRepository.NecessityTotals::getNecessityId,
                                Function.identity()));
    }

    private InstitutionNecessityResponse response(Necessity necessity) {
        return response(necessity, totals(List.of(necessity.getId())).get(necessity.getId()));
    }

    private InstitutionNecessityResponse response(
            Necessity n, DonationMatchRepository.NecessityTotals totals) {
        long received = totals == null ? 0 : totals.getReceivedQuantity();
        long reserved = totals == null ? 0 : totals.getReservedQuantity();
        boolean blocked = totals != null && totals.getBlockingProposals() > 0;
        boolean active = n.getStatus() == NecessityStatus.ACTIVE;
        String restriction =
                blocked
                        ? (received > 0
                                        ? reserved > 0
                                                ? "Há propostas em andamento e concluídas."
                                                : "Há propostas concluídas."
                                        : "Há propostas em andamento.")
                                + " Edição e exclusão bloqueadas."
                        : !active
                                ? "Esta necessidade foi concluída ou cancelada e não pode ser"
                                        + " editada."
                                : null;
        return new InstitutionNecessityResponse(
                n.getId(),
                n.getItemType().getName(),
                icons.resolve(n.getItemType().getCategory()),
                n.getItemType().getCategory(),
                n.getDescription(),
                n.getQuantityRequested(),
                Math.max(0, n.getQuantityRequested() - received - reserved),
                reserved,
                received,
                received + reserved,
                totals != null && totals.getProposalCount() > 0,
                n.getPriority(),
                n.getStatus(),
                active && !blocked,
                active && !blocked,
                restriction,
                n.getVersion());
    }

    @Transactional
    public void create(Integer institutionId, ManageNecessityRequest request) {
        lockPolicy.apply();
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
        lockPolicy.apply();
        var necessity = necessities.findForUpdate(necessityId).orElseThrow(this::missing);
        requireOwner(necessity, institutionId);
        requireManageable(necessity);
        requireVersion(necessity, request.version());
        // Any active or completed proposal blocks editing; cancelled/rejected history is retained.
        necessity.setItemType(itemType(request));
        necessity.setDescription(request.description().strip());
        necessity.setQuantityRequested(request.quantity().intValueExact());
        necessity.setPriority(request.priority());
        necessities.flush();
        return response(necessity);
    }

    @Transactional
    public void delete(Integer institutionId, Integer necessityId, long version) {
        lockPolicy.apply();
        var necessity = necessities.findForUpdate(necessityId).orElseThrow(this::missing);
        requireOwner(necessity, institutionId);
        requireManageable(necessity);
        requireVersion(necessity, version);
        if (matches.existsByNecessityId(necessityId)) {
            // Never cascade-delete cancelled proposals or their outbox/history records.
            necessity.setDeletedAt(Instant.now());
            necessity.setStatus(NecessityStatus.CANCELLED);
        } else necessities.delete(necessity);
    }

    private void requireOwner(Necessity necessity, Integer institutionId) {
        if (necessity.getDeletedAt() != null
                || !necessity.getInstitution().getId().equals(institutionId)) throw missing();
    }

    private void requireManageable(Necessity necessity) {
        if (necessity.getStatus() != NecessityStatus.ACTIVE)
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta necessidade já foi concluída ou cancelada. Atualize a lista para"
                            + " consultar seu estado atual.");
        if (matches.existsByNecessityIdAndStatusNotIn(
                necessity.getId(), Set.of(MatchStatus.CANCELLED, MatchStatus.REJECTED)))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Edição e exclusão exigem que todas as propostas estejam canceladas ou"
                        + " recusadas.");
    }

    private void requireVersion(Necessity necessity, Long version) {
        if (version == null || version != necessity.getVersion())
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta necessidade foi alterada em outra sessão. Seus dados não foram salvos."
                            + " Recarregue os dados antes de editar novamente.");
    }

    private ItemType itemType(ManageNecessityRequest request) {
        // The unique normalized index and UPSERT serialize concurrent spelling variants.
        var id = itemTypes.obtainId(request.category().getValue(), request.description().strip());
        return itemTypes.findById(id).orElseThrow();
    }

    private ResponseStatusException missing() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Necessidade não encontrada.");
    }
}

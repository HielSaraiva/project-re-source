package edu.br.resource.resourcesystem.service.match;

import edu.br.resource.resourcesystem.dto.*;
import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.dto.response.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.presentation.MatchViewAssembler;
import edu.br.resource.resourcesystem.repository.*;
import edu.br.resource.resourcesystem.repository.specification.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchPageService {
    private final UserRepository users;
    private final InstitutionRepository institutions;
    private final NecessityRepository necessities;
    private final DonationRepository donations;
    private final DonationMatchRepository matches;
    private final MatchDtoMapper mapper;
    private final MatchViewAssembler views;

    public Map<String, Object> donorDashboard(Integer donorId) {
        var donor = users.findById(donorId).orElseThrow(this::missing);
        var needs = necessities.findAll(
                NecessitySpecifications.withRemainingQuantity()
                        .and(NecessitySpecifications.withPriority(NecessityPriority.HIGH)),
                PageRequest.of(0, 3, Sort.by("createdAt").descending())).map(mapper::need);
        var shipments = matches
                .findAll(MatchSpecifications.forDonor(donorId),
                        PageRequest.of(0, 5, Sort.by("updatedAt").descending().and(Sort.by("id").descending())))
                .map(mapper::summary);
        return MatchViewAssembler.fields("donorName", donor.getFullName(), "urgencies",
                needs.stream().map(views::need).toList(), "shipments", shipments.stream().map(views::summary).toList());
    }

    public Map<String, Object> needs(Integer donorId, NeedFilterRequest f) {
        var spec = NecessitySpecifications.withRemainingQuantity()
                .and(NecessitySpecifications.withCategory(f.category()))
                .and(NecessitySpecifications.withPriority(f.priority()))
                .and(NecessitySpecifications.inState(f.stateId())).and(NecessitySpecifications.search(f.query()));
        var page = necessities
                .findAll(spec,
                        PageRequest.of(f.page() - 1, f.size(),
                                Sort.by("priority").ascending().and(Sort.by("createdAt").descending())))
                .map(mapper::need);
        return MatchViewAssembler.fields("donorName", users.findById(donorId).orElseThrow(this::missing).getFullName(),
                "needs", page.stream().map(views::need).toList(), "pagination", new PageResponse<>(page.getContent(),
                        f.page(), f.size(), page.getTotalElements(), page.getTotalPages()),
                "filter", f);
    }

    public Map<String, Object> proposal(Integer donorId, Integer needId) {
        var need = necessities.findByIdAndStatus(needId, NecessityStatus.ACTIVE).orElseThrow(this::missing);
        if (need.getInstitution().getStatus() != InstitutionStatus.APPROVED)
            throw missing();
        var inventory = donations
                .findAll(DonationSpecifications.forDonor(donorId)
                        .and(DonationSpecifications.withCategory(need.getItemType().getCategory()))
                        .and(DonationSpecifications.withAvailableQuantity()), Pageable.unpaged())
                .map(mapper::inventory);
        return MatchViewAssembler.fields("donorName", users.findById(donorId).orElseThrow(this::missing).getFullName(),
                "need", views.need(mapper.need(need)), "inventoryDonations",
                inventory.stream().map(views::inventory).toList(), "cancellableDonation", null);
    }

    public Map<String, Object> donorDetail(Integer donorId, String protocol) {
        return views.detail(
                mapper.detail(matches.findByProtocolAndDonationDonorId(protocol, donorId).orElseThrow(this::missing)));
    }

    public Map<String, Object> institutionDetail(Integer institutionId, String protocol) {
        return views.detail(mapper.detail(
                matches.findByProtocolAndNecessityInstitutionId(protocol, institutionId).orElseThrow(this::missing)));
    }

    public Map<String, Object> institutionDashboard(Integer id) {
        var i = institutions.findById(id).orElseThrow(this::missing);
        var pending = matches
                .findAll(
                        MatchSpecifications.forInstitution(id)
                                .and(MatchSpecifications.withStatus(MatchStatus.AWAITING_ACCEPTANCE)),
                        PageRequest.of(0, 3, Sort.by("acceptanceDeadline").ascending()
                                .and(Sort.by("createdAt").ascending()).and(Sort.by("id").ascending())))
                .map(mapper::summary);
        var needs = necessities.findAll(
                NecessitySpecifications.forInstitution(id).and(NecessitySpecifications.withRemainingQuantity()),
                PageRequest.of(0, 6)).map(mapper::need);
        return MatchViewAssembler.fields("ongName", i.getLegalName(), "ongRole", "Administrador", "receivedDonations",
                matches.countByNecessityInstitutionIdAndStatus(id, MatchStatus.COMPLETED), "pendingAcceptances",
                pending.getTotalElements(), "activeNeeds",
                necessities.countByInstitutionIdAndStatus(id, NecessityStatus.ACTIVE), "intentions",
                pending.stream().map(views::summary).toList(), "needs", needs.stream().map(views::need).toList());
    }

    public Map<String, Object> institutionMatches(Integer id, MatchFilterRequest f) {
        Page<edu.br.resource.resourcesystem.model.entity.DonationMatch> entities = f.order() == MatchSortOrder.DEADLINE
                ? matches.findForInstitutionOrderedByDeadline(id, f.status(),
                        f.query() == null || f.query().isBlank() ? null : SearchPatterns.contains(f.query()),
                        PageRequest.of(f.page() - 1, f.size()))
                : matches.findAll(
                        MatchSpecifications.forInstitution(id).and(MatchSpecifications.withStatus(f.status()))
                                .and(MatchSpecifications.search(f.query())),
                        PageRequest.of(f.page() - 1, f.size(),
                                Sort.by("createdAt").descending().and(Sort.by("id").descending())));
        var page = entities.map(mapper::summary);
        return MatchViewAssembler.fields("ongName", institutions.findById(id).orElseThrow(this::missing).getLegalName(),
                "ongRole", "Administrador", "intentions", page.stream().map(views::summary).toList(),
                "pendingAcceptances",
                matches.countByNecessityInstitutionIdAndStatus(id, MatchStatus.AWAITING_ACCEPTANCE), "pagination",
                new PageResponse<>(page.getContent(), f.page(), f.size(), page.getTotalElements(),
                        page.getTotalPages()),
                "filter", f);
    }

    public String canonicalPath(MatchDetailResponse match) {
        return views.donorPath(match.status(), match.delivery() == null ? null : match.delivery().method());
    }

    private ResponseStatusException missing() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado.");
    }
}

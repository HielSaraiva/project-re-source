package edu.br.resource.resourcesystem.service;

import edu.br.resource.resourcesystem.dto.request.DonationHistoryFilterRequest;
import edu.br.resource.resourcesystem.dto.response.*;
import edu.br.resource.resourcesystem.model.entity.Donation;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.presentation.CategoryIconResolver;
import edu.br.resource.resourcesystem.repository.*;
import edu.br.resource.resourcesystem.repository.specification.DonationSpecifications;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DonationHistoryService {
    private final DonationRepository donations;
    private final DonationMatchRepository matches;
    private final CategoryIconResolver icons;

    public Page<DonationHistoryResponse> findDonationsByDonor(
            Integer donorId, DonationHistoryFilterRequest filter) {
        var spec =
                DonationSpecifications.inventory(donorId, filter.archived())
                        .and(DonationSpecifications.withCategory(filter.category()))
                        .and(
                                DonationSpecifications.withStatus(
                                        filter.completed()
                                                ? DonationStatus.COMPLETED
                                                : filter.archived() ? null : filter.status()))
                        .and(DonationSpecifications.search(filter.query()))
                        .and(
                                (root, query, builder) ->
                                        filter.historical()
                                                ? builder.conjunction()
                                                : builder.notEqual(
                                                        root.get("status"),
                                                        DonationStatus.COMPLETED));
        var sort = Sort.by("createdAt").descending().and(Sort.by("id").descending());
        var page =
                donations.findAll(
                        spec,
                        PageRequest.of(
                                filter.page() - 1, DonationHistoryFilterRequest.PAGE_SIZE, sort));
        if (page.isEmpty() && page.getTotalPages() > 0 && filter.page() > 1)
            page =
                    donations.findAll(
                            spec,
                            PageRequest.of(
                                    page.getTotalPages() - 1,
                                    DonationHistoryFilterRequest.PAGE_SIZE,
                                    sort));
        var totals = totals(page.getContent().stream().map(Donation::getId).toList());
        return page.map(d -> response(d, totals.get(d.getId())));
    }

    public DonationHistoryResponse details(Integer donorId, Integer id) {
        var donation = owned(donorId, id);
        if (donation.getDeletedAt() != null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Doação não encontrada.");
        return response(donation, totals(List.of(id)).get(id));
    }

    public static List<MatchStatus> proposalStatuses() {
        return List.of(
                MatchStatus.AWAITING_ACCEPTANCE,
                MatchStatus.AWAITING_SHIPMENT,
                MatchStatus.AWAITING_DELIVERY,
                MatchStatus.IN_TRANSIT,
                MatchStatus.AWAITING_NGO_CONFIRMATION,
                MatchStatus.COMPLETED,
                MatchStatus.REJECTED,
                MatchStatus.CANCELLED);
    }

    public PageResponse<DonationProposalResponse> proposals(Integer donorId, Integer id, int page) {
        return proposals(donorId, id, page, null);
    }

    public PageResponse<DonationProposalResponse> proposals(
            Integer donorId, Integer id, int requestedPage, MatchStatus status) {
        owned(donorId, id); // Historical protocols remain readable after logical deletion.
        if (requestedPage < 1)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Página inválida.");
        if (status != null && !proposalStatuses().contains(status))
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Status de proposta inválido.");
        var sort = Sort.by("createdAt").descending().and(Sort.by("id").descending());
        var page = proposalPage(donorId, id, requestedPage, sort, status);
        if (page.isEmpty() && page.getTotalPages() > 0 && requestedPage > 1)
            page = proposalPage(donorId, id, page.getTotalPages(), sort, status);
        var result =
                page.map(
                        m ->
                                new DonationProposalResponse(
                                        m.getProtocol(),
                                        m.getNecessity().getInstitution().getLegalName(),
                                        m.getAllocatedQuantity(),
                                        m.getStatus(),
                                        m.getStatus().getLabel(),
                                        m.getCreatedAt()));
        return new PageResponse<>(
                result.getContent(),
                result.getNumber() + 1,
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    private Page<edu.br.resource.resourcesystem.model.entity.DonationMatch> proposalPage(
            Integer donorId, Integer id, int page, Sort sort, MatchStatus status) {
        var pageable = PageRequest.of(page - 1, 8, sort);
        if (status == null)
            return matches.findByDonationIdAndDonationDonorId(id, donorId, pageable);
        var states =
                switch (status) {
                    case AWAITING_ACCEPTANCE ->
                            Set.of(MatchStatus.PROPOSED, MatchStatus.AWAITING_ACCEPTANCE);
                    case AWAITING_SHIPMENT ->
                            Set.of(MatchStatus.ACCEPTED, MatchStatus.AWAITING_SHIPMENT);
                    default -> Set.of(status);
                };
        return matches.findByDonationIdAndDonationDonorIdAndStatusIn(id, donorId, states, pageable);
    }

    private Donation owned(Integer donorId, Integer id) {
        return donations
                .findByIdAndDonorId(id, donorId)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Doação não encontrada."));
    }

    private Map<Integer, DonationMatchRepository.InventoryTotals> totals(List<Integer> ids) {
        if (ids.isEmpty()) return Map.of();
        return matches
                .summarizeInventory(
                        ids, MatchStatus.COMPLETED, MatchStatus.REJECTED, MatchStatus.CANCELLED)
                .stream()
                .collect(
                        Collectors.toMap(
                                DonationMatchRepository.InventoryTotals::getDonationId,
                                Function.identity()));
    }

    private DonationHistoryResponse response(
            Donation d, DonationMatchRepository.InventoryTotals totals) {
        var item = d.getDonationPackage().getItem();
        int quantity = d.getDonationPackage().getQuantity();
        long reserved = totals == null ? 0 : totals.getReservedQuantity(),
                received = totals == null ? 0 : totals.getReceivedQuantity();
        long active = totals == null ? 0 : totals.getActiveCount(),
                count = totals == null ? 0 : totals.getProposalCount();
        String editReason =
                d.getStatus() == DonationStatus.COMPLETED
                        ? "Doação concluída: a edição não está disponível."
                        : active > 0
                                ? "A edição fica bloqueada enquanto houver propostas em andamento."
                                : null;
        String deleteReason =
                active > 0
                        ? (received > 0
                                ? "Há propostas em andamento e concluídas. A exclusão fica"
                                        + " bloqueada."
                                : "Há propostas em andamento. A exclusão fica bloqueada.")
                        : received > 0
                                ? "Há propostas concluídas. A exclusão fica bloqueada."
                                : null;
        boolean archived = d.getDeletedAt() != null;
        return new DonationHistoryResponse(
                d.getId(),
                item.getTitle(),
                icons.resolve(item.getItemType().getCategory()),
                item.getItemType().getCategory(),
                item.getItemType().getCategory().getLabel(),
                item.getCondition(),
                item.getDescription(),
                quantity,
                Math.max(0, quantity - reserved - received),
                reserved,
                received,
                d.getCreatedAt(),
                d.getStatus(),
                archived ? "Excluída" : label(d.getStatus()),
                count,
                active,
                !archived && editReason == null,
                !archived && deleteReason == null,
                editReason,
                deleteReason,
                d.getEditVersion(),
                archived);
    }

    public static String label(DonationStatus status) {
        return switch (status) {
            case REGISTERED -> "Registrada";
            case COMPLETED -> "Concluída";
            case IN_TRANSIT -> "Enviada";
            case REJECTED -> "Recusada";
            case ACCEPTED -> "Aceita";
            default -> status.getLabel();
        };
    }
}

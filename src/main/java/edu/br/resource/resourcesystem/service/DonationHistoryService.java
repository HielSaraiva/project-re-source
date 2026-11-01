package edu.br.resource.resourcesystem.service;

import edu.br.resource.resourcesystem.dto.response.DonationHistoryResponse;
import edu.br.resource.resourcesystem.model.entity.Donation;
import edu.br.resource.resourcesystem.model.entity.DonationMatch;
import edu.br.resource.resourcesystem.model.enums.MatchStatus;
import edu.br.resource.resourcesystem.presentation.CategoryIconResolver;
import edu.br.resource.resourcesystem.repository.DonationMatchRepository;
import edu.br.resource.resourcesystem.repository.DonationRepository;
import edu.br.resource.resourcesystem.service.match.DonationStatusPolicy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DonationHistoryService {
    private static final Comparator<DonationMatch> NEWEST_FIRST =
            Comparator.comparing(DonationMatch::getCreatedAt)
                    .thenComparing(DonationMatch::getId)
                    .reversed();

    private final DonationRepository donationRepository;
    private final DonationMatchRepository matchRepository;
    private final CategoryIconResolver icons;

    public DonationHistoryService(
            DonationRepository donationRepository,
            DonationMatchRepository matchRepository,
            CategoryIconResolver icons) {
        this.donationRepository = donationRepository;
        this.matchRepository = matchRepository;
        this.icons = icons;
    }

    public Page<DonationHistoryResponse> findDonationsByDonor(Integer donorId, Pageable pageable) {
        Page<Donation> page = donationRepository.findByDonorId(donorId, pageable);
        Map<Integer, List<DonationMatch>> matchesByDonation =
                page.isEmpty()
                        ? Map.of()
                        : matchRepository
                                .findAllByDonationIdIn(
                                        page.map(Donation::getId).getContent())
                                .stream()
                                .collect(
                                        Collectors.groupingBy(
                                                match -> match.getDonation().getId()));
        return page.map(
                donation ->
                        toResponse(donation, matchesByDonation.getOrDefault(donation.getId(), List.of())));
    }

    DonationHistoryResponse toResponse(Donation donation, Collection<DonationMatch> matches) {
        var item = donation.getDonationPackage().getItem();
        int quantity = donation.getDonationPackage().getQuantity();
        long available = Math.max(0, quantity - DonationStatusPolicy.allocatedQuantity(matches));
        long activeCount = DonationStatusPolicy.activeCount(matches);

        DonationMatch highlighted =
                DonationStatusPolicy.oldestActive(matches)
                        .orElseGet(
                                () ->
                                        matches.stream()
                                                .filter(m -> m.getStatus() == MatchStatus.COMPLETED)
                                                .min(NEWEST_FIRST)
                                                .orElse(null));

        String recipient = null;
        String protocol = null;
        String caption = null;
        String actionLabel = available > 0 && activeCount == 0 ? "Buscar ONGs" : null;
        String actionUrl = actionLabel == null ? null : "/donor/needs";
        long others = 0;

        if (highlighted != null) {
            boolean active = MatchStatus.activeStatuses().contains(highlighted.getStatus());
            recipient = highlighted.getNecessity().getInstitution().getLegalName();
            protocol = highlighted.getProtocol();
            caption =
                    (active ? "Proposta " : "Última proposta ")
                            + protocol
                            + " · "
                            + highlighted.getStatus().getLabel();
            others = Math.max(0, activeCount - 1);
            actionLabel =
                    highlighted.getStatus() == MatchStatus.AWAITING_SHIPMENT
                            ? "Continuar envio"
                            : "Ver detalhes";
            actionUrl = "/donor/donation/status?protocol=" + protocol;
        }

        return new DonationHistoryResponse(
                donation.getId(),
                item.getTitle(),
                icons.resolve(item.getItemType().getCategory()),
                item.getItemType().getCategory().getLabel(),
                item.getCondition().getValue(),
                item.getDescription(),
                quantity,
                available,
                donation.getCreatedAt(),
                donation.getStatus().getValue(),
                donation.getStatus().getLabel(),
                recipient,
                protocol,
                caption,
                others,
                actionLabel,
                actionUrl,
                DonationManagementService.canEdit(donation.getStatus(), matches),
                DonationManagementService.canDelete(matches));
    }
}

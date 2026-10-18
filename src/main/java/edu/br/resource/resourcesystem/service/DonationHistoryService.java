
package edu.br.resource.resourcesystem.service;

import edu.br.resource.resourcesystem.dto.response.DonationHistoryResponse;
import edu.br.resource.resourcesystem.model.entity.Donation;
import edu.br.resource.resourcesystem.model.entity.DonationMatch;
import edu.br.resource.resourcesystem.model.enums.DonationStatus;
import edu.br.resource.resourcesystem.model.enums.MatchStatus;
import edu.br.resource.resourcesystem.repository.DonationMatchRepository;
import edu.br.resource.resourcesystem.repository.DonationRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DonationHistoryService {

    private final DonationRepository donationRepository;
    private final DonationMatchRepository matchRepository;

    // Construtor explícito, sem Lombok.
    public DonationHistoryService(
            DonationRepository donationRepository,
            DonationMatchRepository matchRepository) {

        this.donationRepository = donationRepository;
        this.matchRepository = matchRepository;
    }

    public Page<DonationHistoryResponse> findDonationsByDonor(
            Integer donorId,
            Pageable pageable) {

        return donationRepository
                .findByDonorId(donorId, pageable)
                .map(this::toResponse);
    }

    private DonationHistoryResponse toResponse(Donation donation) {

        List<DonationMatch> matches =
                matchRepository.findAllByDonationId(donation.getId());

        // Prioriza matches ativos.
        DonationMatch relevantMatch = matches.stream()
                .filter(match ->
                        MatchStatus.activeStatuses()
                                .contains(match.getStatus()))
                .max(Comparator
                        .comparing(DonationMatch::getCreatedAt)
                        .thenComparing(DonationMatch::getId))
                .orElse(null);

        // Se não houver match ativo, procura um concluído.
        if (relevantMatch == null) {
            relevantMatch = matches.stream()
                    .filter(match ->
                            match.getStatus() == MatchStatus.COMPLETED)
                    .max(Comparator
                            .comparing(DonationMatch::getCreatedAt)
                            .thenComparing(DonationMatch::getId))
                    .orElse(null);
        }

        String recipientName = "—";
        String status = donation.getStatus().getValue();
        String statusLabel = donation.getStatus().getLabel();

        String actionLabel = "Buscar ONGs";
        String actionUrl = "/donor/needs";

        if (relevantMatch != null) {

            recipientName = relevantMatch
                    .getNecessity()
                    .getInstitution()
                    .getLegalName();

            MatchStatus matchStatus = relevantMatch.getStatus();

            status = matchStatus.getValue();
            statusLabel = matchStatus.getLabel();

            if (matchStatus == MatchStatus.AWAITING_SHIPMENT
                    || matchStatus == MatchStatus.ACCEPTED) {

                actionLabel = "Opções de envio";

            } else {
                actionLabel = "Ver detalhes";
            }

            actionUrl = "/donor/donation/status?protocol="
                    + relevantMatch.getProtocol();
        }

        // Verifica se há quantidades comprometidas em matches.
        boolean hasAllocatedQuantity = matches.stream()
                .anyMatch(match ->
                        MatchStatus.allocationConsumingStatuses()
                                .contains(match.getStatus()));

        // Uma doação registrada só pode ser editada
        // quando não possui quantidades comprometidas.
        boolean editable =
                donation.getStatus() == DonationStatus.REGISTERED
                        && !hasAllocatedQuantity;

        return new DonationHistoryResponse(
                donation.getId(),
                donation.getDonationPackage()
                        .getItem()
                        .getTitle(),
                recipientName,
                donation.getCreatedAt(),
                status,
                statusLabel,
                actionLabel,
                actionUrl,
                editable
        );
    }
}

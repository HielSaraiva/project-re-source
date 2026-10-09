package edu.br.resource.resourcesystem.service.match;

import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.dto.response.*;
import edu.br.resource.resourcesystem.model.entity.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.repository.*;
import edu.br.resource.resourcesystem.messaging.MatchEventOutbox;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(noRollbackFor = WorkflowConflictException.class)
public class MatchWorkflowService {
    private static final Duration DEADLINE = Duration.ofDays(7);
    private final MatchLockPolicy lockPolicy;
    private final UserRepository users;
    private final InstitutionRepository institutions;
    private final ItemRepository items;
    private final DonationPackageRepository packages;
    private final DonationRepository donations;
    private final NecessityRepository necessities;
    private final DonationMatchRepository matches;
    private final DeliveryRepository deliveries;
    private final MatchStatusHistoryRepository history;
    private final DonationStatusHistoryRepository donationHistory;
    private final MatchDtoMapper mapper;
    private final MatchEventOutbox outbox;
    private final MatchProtocolRepository protocols;
    private final jakarta.persistence.EntityManager entityManager;

    public InventoryDonationResponse register(Integer donorId, @Valid RegisterDonationRequest request) {
        var need = necessities.findByIdAndStatus(request.necessityId(), NecessityStatus.ACTIVE)
                .orElseThrow(this::missing);
        if (need.getInstitution().getStatus() != InstitutionStatus.APPROVED)
            throw conflict("Instituição indisponível.");
        var donor = users.findById(donorId).orElseThrow(this::missing);
        var item = items.save(Item.builder().itemType(need.getItemType()).title(request.title().strip())
                .condition(request.condition()).description(clean(request.description())).build());
        var pack = packages.save(DonationPackage.builder().item(item).quantity(request.quantity()).build());
        var donation = donations.saveAndFlush(Donation.builder().donor(donor).donationPackage(pack).build());
        donationHistory.save(DonationStatusHistory.builder().donation(donation).newStatus(DonationStatus.REGISTERED)
                .changedByUser(donor).notes("Itens cadastrados para doação.").build());
        return mapper.inventory(donation);
    }

    public MatchDetailResponse propose(Integer donorId, @Valid SubmitProposalRequest request) {
        lockPolicy.apply();
        var donation = donations.findForUpdate(request.donationId(), donorId).orElseThrow(this::missing);
        var need = necessities.findForUpdate(request.necessityId()).orElseThrow(this::missing);
        if (need.getStatus() != NecessityStatus.ACTIVE
                || need.getInstitution().getStatus() != InstitutionStatus.APPROVED)
            throw conflict("Esta necessidade não está disponível.");
        if (!donation.getDonationPackage().getItem().getItemType().getCategory()
                .equals(need.getItemType().getCategory()))
            throw conflict("A categoria do item deve corresponder à necessidade.");
        long available = donation.getDonationPackage().getQuantity()
                - matches.sumAllocatedByDonation(donation.getId(), MatchStatus.allocationConsumingStatuses());
        long remaining = need.getQuantityRequested()
                - matches.sumAllocatedByNecessity(need.getId(), MatchStatus.allocationConsumingStatuses());
        if (request.quantity() > available || request.quantity() > remaining)
            throw conflict("A quantidade excede o saldo disponível. Atualize a página.");
        if (matches.existsByDonationIdAndNecessityIdAndStatusIn(donation.getId(), need.getId(),
                MatchStatus.activeStatuses()))
            throw conflict("Já existe uma proposta ativa para esta doação e necessidade.");
        Instant now = Instant.now();
        long number = protocols.nextNumber();
        var match = matches.saveAndFlush(DonationMatch.builder().donation(donation).necessity(need)
                .allocatedQuantity(request.quantity()).status(MatchStatus.AWAITING_ACCEPTANCE)
                .acceptanceDeadline(now.plus(DEADLINE)).proposalMessage(clean(request.message()))
                .protocol("INT-" + now.atZone(MatchDtoMapper.ZONE).getYear() + "-" + number).build());
        event(match, null, MatchEventType.PROPOSAL_SUBMITTED, donation.getDonor(), null,
                "Itens e quantidade oferecidos à ONG.");
        synchronizeDonation(donation, donation.getDonor(), null);
        return mapper.detail(match);
    }

    public MatchDetailResponse accept(Integer institutionId, String protocol) {
        var m = lock(protocol, null, institutionId);
        requireLive(m);
        require(m, MatchStatus.AWAITING_ACCEPTANCE);
        var old = m.getStatus();
        m.setStatus(MatchStatus.AWAITING_SHIPMENT);
        m.setAcceptedAt(Instant.now());
        m.setDeliveryMethodDeadline(m.getAcceptedAt().plus(DEADLINE));
        event(m, old, MatchEventType.PROPOSAL_ACCEPTED, null, m.getNecessity().getInstitution(),
                "O doador tem 7 dias para escolher a modalidade de entrega.");
        synchronizeDonation(m.getDonation(), null, m.getNecessity().getInstitution());
        return mapper.detail(m);
    }

    public MatchDetailResponse reject(Integer institutionId, String protocol, String reason) {
        if (reason == null || reason.isBlank() || reason.length() > 1000)
            throw conflict("Informe o motivo da recusa, com até 1.000 caracteres.");
        var m = lock(protocol, null, institutionId);
        requireLive(m);
        require(m, MatchStatus.AWAITING_ACCEPTANCE);
        var old = m.getStatus();
        m.setStatus(MatchStatus.REJECTED);
        m.setRejectedAt(Instant.now());
        m.setRejectionReason(reason.strip());
        event(m, old, MatchEventType.PROPOSAL_REJECTED, null, m.getNecessity().getInstitution(), reason.strip());
        synchronizeDonation(m.getDonation(), null, m.getNecessity().getInstitution());
        return mapper.detail(m);
    }

    public MatchDetailResponse cancel(Integer donorId, String protocol) {
        var m = lock(protocol, donorId, null);
        requireLive(m);
        if (!MatchStatus.activeStatuses().contains(m.getStatus()) || m.getStatus() == MatchStatus.IN_TRANSIT
                || m.getStatus() == MatchStatus.AWAITING_NGO_CONFIRMATION)
            throw conflict("Esta doação não pode mais ser cancelada.");
        cancelMatch(m, "Cancelamento solicitado pelo doador.", null, m.getDonation().getDonor());
        return mapper.detail(m);
    }

    public MatchDetailResponse selectMethod(Integer donorId, String protocol, DeliveryMethod method) {
        if (method != DeliveryMethod.IN_PERSON && method != DeliveryMethod.CARRIER)
            throw conflict("Modalidade indisponível.");
        var m = lock(protocol, donorId, null);
        requireLive(m);
        if (m.getStatus() != MatchStatus.AWAITING_SHIPMENT && m.getStatus() != MatchStatus.ACCEPTED)
            throw conflict("Esta doação não está aguardando a escolha da modalidade.");
        if (deliveries.findByMatchId(m.getId()).isPresent())
            throw conflict("A modalidade já foi confirmada e não pode ser alterada.");
        var recipient = mapper.recipient(m.getNecessity().getInstitution());
        if (recipient.address() == null)
            throw conflict("A ONG precisa cadastrar o endereço de recebimento antes da entrega.");
        var a = recipient.address();
        var now = Instant.now();
        var d = Delivery.builder().match(m).method(method).methodConfirmedAt(now)
                .deliveryAddress(a.street() + ", " + a.number() + (a.complement() == null ? "" : " — " + a.complement())
                        + ", " + a.district() + ", " + a.location().city() + "/" + a.location().state() + ", CEP "
                        + a.postalCode())
                .build();
        if (method == DeliveryMethod.IN_PERSON)
            d.setInPersonDeadline(now.plus(DEADLINE));
        else {
            d.setShippingDeadline(now.plus(DEADLINE));
            d.setCarrierName("Correios");
        }
        deliveries.saveAndFlush(d);
        var old = m.getStatus();
        m.setStatus(method == DeliveryMethod.IN_PERSON ? MatchStatus.AWAITING_DELIVERY : MatchStatus.AWAITING_SHIPMENT);
        event(m, old, MatchEventType.DELIVERY_METHOD_SELECTED, m.getDonation().getDonor(), null,
                method.getLabel() + " confirmada. Prazo de 7 dias para informar a entrega ou postagem.");
        synchronizeDonation(m.getDonation(), m.getDonation().getDonor(), null);
        return mapper.detail(m);
    }

    public MatchDetailResponse reportDelivery(Integer donorId, String protocol,
            @Valid ConfirmInPersonDeliveryRequest request) {
        var m = lock(protocol, donorId, null);
        requireLive(m);
        require(m, MatchStatus.AWAITING_DELIVERY);
        var d = deliveries.findForUpdateByMatchId(m.getId()).orElseThrow(this::missing);
        if (d.getMethod() != DeliveryMethod.IN_PERSON)
            throw conflict("Modalidade incompatível.");
        requirePendingDelivery(d);
        validateDate(request.date(), d);
        d.setDeliveredAt(request.date().atStartOfDay(MatchDtoMapper.ZONE).toInstant());
        d.setReportedAt(Instant.now());
        d.setReceivedByName(request.recipient().strip());
        d.setDeliveryNotes(clean(request.notes()));
        d.setStatus(DeliveryStatus.AWAITING_CONFIRMATION);
        var old = m.getStatus();
        m.setStatus(MatchStatus.AWAITING_NGO_CONFIRMATION);
        event(m, old, MatchEventType.DELIVERY_REPORTED, m.getDonation().getDonor(), null,
                "Entrega presencial informada. Aguarda conferência e confirmação da ONG.");
        synchronizeDonation(m.getDonation(), m.getDonation().getDonor(), null);
        return mapper.detail(m);
    }

    public MatchDetailResponse reportShipment(Integer donorId, String protocol,
            @Valid ConfirmCarrierShipmentRequest request) {
        var m = lock(protocol, donorId, null);
        requireLive(m);
        require(m, MatchStatus.AWAITING_SHIPMENT);
        var d = deliveries.findForUpdateByMatchId(m.getId()).orElseThrow(this::missing);
        if (d.getMethod() != DeliveryMethod.CARRIER)
            throw conflict("Modalidade incompatível.");
        requirePendingDelivery(d);
        validateDate(request.date(), d);
        d.setShippedAt(request.date().atStartOfDay(MatchDtoMapper.ZONE).toInstant());
        d.setReportedAt(Instant.now());
        d.setTrackingCode(request.trackingCode().toUpperCase(Locale.ROOT));
        d.setDeliveryNotes(clean(request.notes()));
        d.setStatus(DeliveryStatus.IN_TRANSIT);
        var old = m.getStatus();
        m.setStatus(MatchStatus.IN_TRANSIT);
        event(m, old, MatchEventType.SHIPMENT_REPORTED, m.getDonation().getDonor(), null,
                "Postagem informada. Rastreamento: " + d.getTrackingCode());
        synchronizeDonation(m.getDonation(), m.getDonation().getDonor(), null);
        return mapper.detail(m);
    }

    public MatchDetailResponse receive(Integer institutionId, String protocol) {
        var m = lock(protocol, null, institutionId);
        if (m.getStatus() != MatchStatus.IN_TRANSIT && m.getStatus() != MatchStatus.AWAITING_NGO_CONFIRMATION)
            throw conflict("O doador ainda não informou a entrega ou postagem, ou o recebimento já foi confirmado.");
        var d = deliveries.findForUpdateByMatchId(m.getId()).orElseThrow(this::missing);
        boolean postal = m.getStatus() == MatchStatus.IN_TRANSIT && d.getMethod() == DeliveryMethod.CARRIER
                && d.getStatus() == DeliveryStatus.IN_TRANSIT;
        boolean personal = m.getStatus() == MatchStatus.AWAITING_NGO_CONFIRMATION
                && d.getMethod() == DeliveryMethod.IN_PERSON && d.getStatus() == DeliveryStatus.AWAITING_CONFIRMATION;
        if (!postal && !personal)
            throw conflict("Os dados da entrega não permitem confirmar o recebimento. Atualize a página.");
        var now = Instant.now();
        d.setStatus(DeliveryStatus.DELIVERED);
        if (d.getDeliveredAt() == null)
            d.setDeliveredAt(now);
        d.setReceiptConfirmedAt(now);
        d.setReceiptConfirmedBy(m.getNecessity().getInstitution());
        var old = m.getStatus();
        m.setStatus(MatchStatus.COMPLETED);
        m.setCompletedAt(now);
        event(m, old, MatchEventType.RECEIPT_CONFIRMED, null, m.getNecessity().getInstitution(),
                "Todos os itens e a quantidade foram conferidos e recebidos pela ONG.");
        if (matches.sumAllocatedByNecessity(m.getNecessity().getId(), Set.of(MatchStatus.COMPLETED)) >= m.getNecessity()
                .getQuantityRequested())
            m.getNecessity().setStatus(NecessityStatus.FULFILLED);
        synchronizeDonation(m.getDonation(), null, m.getNecessity().getInstitution());
        return mapper.detail(m);
    }

    public boolean expire(String protocol) {
        var m = lock(protocol, null, null);
        return expireIfDue(m);
    }

    private DonationMatch lock(String protocol, Integer donorId, Integer institutionId) {
        lockPolicy.apply();
        var found = (donorId != null ? matches.findByProtocolAndDonationDonorId(protocol, donorId)
                : institutionId != null ? matches.findByProtocolAndNecessityInstitutionId(protocol, institutionId)
                        : matches.findByProtocol(protocol))
                .orElseThrow(this::missing);
        // All mutations acquire shared resources in donation -> necessity -> match
        // order.
        donations.findForUpdate(found.getDonation().getId(), found.getDonation().getDonor().getId())
                .orElseThrow(this::missing);
        necessities.findForUpdate(found.getNecessity().getId()).orElseThrow(this::missing);
        var m = matches.findForUpdate(protocol).orElseThrow(this::missing);
        entityManager.refresh(m, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        entityManager.refresh(m.getDonation());
        entityManager.refresh(m.getNecessity());
        return m;
    }

    private void requireLive(DonationMatch m) {
        if (expireIfDue(m))
            throw conflict("O prazo terminou. A doação foi cancelada automaticamente e a reserva foi liberada.");
    }

    private boolean expireIfDue(DonationMatch m) {
        var d = deliveries.findByMatchId(m.getId()).orElse(null);
        var deadlines = mapper.deadlines(m, d);
        if (deadlines.activeDeadline() == null || Instant.now().isBefore(deadlines.activeDeadline()))
            return false;
        cancelMatch(m, "Prazo de 7 dias encerrado: " + deadlines.activeStage().getLabel() + ".",
                deadlines.activeStage(), null);
        return true;
    }

    private void cancelMatch(DonationMatch m, String reason, DonationStage stage, User actor) {
        var old = m.getStatus();
        m.setStatus(MatchStatus.CANCELLED);
        m.setCancelledAt(Instant.now());
        m.setCancellationReason(reason);
        m.setExpiredStage(stage);
        deliveries.findForUpdateByMatchId(m.getId()).ifPresent(d -> d.setStatus(DeliveryStatus.CANCELLED));
        event(m, old, MatchEventType.DONATION_CANCELLED, actor, null, reason);
        synchronizeDonation(m.getDonation(), actor, null);
    }

    private void event(DonationMatch m, MatchStatus previous, MatchEventType type, User user, Institution institution,
            String notes) {
        matches.saveAndFlush(m);
        // Choosing Correios creates an event while the match keeps the same status.
        // Update its timestamp even when Hibernate sees no changed match fields.
        matches.recordFlowUpdate(m.getId());
        entityManager.refresh(m);
        history.saveAndFlush(MatchStatusHistory.builder().match(m).previousStatus(previous).newStatus(m.getStatus())
                .changedByUser(user).changedByInstitution(institution)
                .actorName(user != null ? user.getFullName()
                        : institution != null ? institution.getLegalName() : "Sistema")
                .eventType(type).eventTitle(type.getLabel()).notes(notes).build());
        outbox.append(m, type);
    }

    private void synchronizeDonation(Donation d, User user, Institution institution) {
        var all = matches.findAllByDonationId(d.getId());
        var active = all.stream().filter(m -> MatchStatus.activeStatuses().contains(m.getStatus()))
                .min(Comparator.comparing(DonationMatch::getCreatedAt));
        long used = matches.sumAllocatedByDonation(d.getId(), MatchStatus.allocationConsumingStatuses());
        var next = active
                .map(m -> DonationStatus.fromValue(
                        m.getStatus() == MatchStatus.PROPOSED ? "awaiting_acceptance" : m.getStatus().getValue()))
                .orElse(used >= d.getDonationPackage().getQuantity() ? DonationStatus.COMPLETED
                        : DonationStatus.REGISTERED);
        if (d.getStatus() != next) {
            var previous = d.getStatus();
            d.setStatus(next);
            donationHistory.save(DonationStatusHistory.builder().donation(d).previousStatus(previous).newStatus(next)
                    .changedByUser(user).changedByInstitution(institution)
                    .notes("Status consolidado das propostas vinculadas à doação.").build());
        }
    }

    private void requirePendingDelivery(Delivery delivery) {
        if (delivery.getStatus() != DeliveryStatus.PENDING || delivery.getReportedAt() != null)
            throw conflict("Esta entrega já foi informada ou não está mais disponível. Atualize a página.");
    }

    private void validateDate(LocalDate date, Delivery d) {
        if (date.isBefore(d.getMethodConfirmedAt().atZone(MatchDtoMapper.ZONE).toLocalDate())
                || date.isAfter(LocalDate.now(MatchDtoMapper.ZONE)))
            throw conflict("A data deve estar entre a escolha da modalidade e hoje.");
    }

    private void require(DonationMatch m, MatchStatus status) {
        if (m.getStatus() != status)
            throw conflict("Esta ação não está disponível no status atual. Atualize a página.");
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private WorkflowConflictException conflict(String text) {
        return new WorkflowConflictException(text);
    }

    private ResponseStatusException missing() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado.");
    }
}

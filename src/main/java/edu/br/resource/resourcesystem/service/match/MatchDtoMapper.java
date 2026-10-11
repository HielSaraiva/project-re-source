package edu.br.resource.resourcesystem.service.match;

import edu.br.resource.resourcesystem.dto.response.*;
import edu.br.resource.resourcesystem.model.entity.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.presentation.CategoryIconResolver;
import edu.br.resource.resourcesystem.repository.*;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchDtoMapper {
    public static final ZoneId ZONE = ZoneId.of("America/Fortaleza");
    private final CategoryIconResolver icons;
    private final DonationMatchRepository matches;
    private final DeliveryRepository deliveries;
    private final InstitutionAddressRepository addresses;
    private final BusinessHoursRepository hours;
    private final MatchStatusHistoryRepository history;

    public ProfileResponse profile(User u) {
        return profile(u.getId(), u.getFullName(), "Doador");
    }

    public ProfileResponse profile(Institution i) {
        return profile(i.getId(), i.getLegalName(), "Administrador");
    }

    private ProfileResponse profile(Integer id, String name, String role) {
        var words = name.strip().split("\\s+");
        String initials =
                words[0].substring(0, 1)
                        + (words.length > 1 ? words[words.length - 1].substring(0, 1) : "");
        return new ProfileResponse(id, name, role, initials.toUpperCase(Locale.ROOT));
    }

    public LocationResponse location(City city) {
        if (city == null) return null;
        return new LocationResponse(
                city.getId(),
                city.getName(),
                city.getState().getAbbreviation() == null
                        ? city.getState().getName()
                        : city.getState().getAbbreviation(),
                city.getLatitude(),
                city.getLongitude());
    }

    public RecipientResponse recipient(Institution i) {
        var address =
                addresses
                        .findByInstitutionId(i.getId())
                        .map(
                                a ->
                                        new AddressResponse(
                                                a.getPostalCode(),
                                                a.getStreet(),
                                                a.getNumber(),
                                                a.getComplement(),
                                                a.getDistrict(),
                                                location(a.getCity())))
                        .orElse(null);
        var receiving =
                hours.findAllByInstitutionIdOrderByOpeningHourAsc(i.getId()).stream()
                        .map(
                                h -> {
                                    var d = h.getOpenDays();
                                    boolean[] flags = {
                                        d.isMonday(),
                                        d.isTuesday(),
                                        d.isWednesday(),
                                        d.isThursday(),
                                        d.isFriday(),
                                        d.isSaturday(),
                                        d.isSunday()
                                    };
                                    List<DayOfWeek> days = new ArrayList<>();
                                    for (int n = 0; n < 7; n++)
                                        if (flags[n]) days.add(DayOfWeek.of(n + 1));
                                    return new ReceivingHoursResponse(
                                            days,
                                            h.getOpeningHour(),
                                            h.getClosingHour(),
                                            h.getResponsibleName());
                                })
                        .toList();
        return new RecipientResponse(
                i.getId(),
                i.getLegalName(),
                i.getCnpj(),
                i.getEmail(),
                i.getPhone(),
                address,
                receiving);
    }

    public InventoryDonationResponse inventory(Donation d) {
        var p = d.getDonationPackage();
        var item = p.getItem();
        long used =
                matches.sumAllocatedByDonation(
                        d.getId(), MatchStatus.allocationConsumingStatuses());
        return new InventoryDonationResponse(
                d.getId(),
                item.getTitle(),
                icons.resolve(item.getItemType().getCategory()),
                item.getItemType().getCategory(),
                item.getCondition(),
                item.getDescription(),
                p.getQuantity(),
                Math.max(0, p.getQuantity() - used),
                d.getStatus());
    }

    public NeedSummaryResponse need(Necessity n) {
        var location =
                addresses
                        .findByInstitutionId(n.getInstitution().getId())
                        .map(address -> location(address.getCity()))
                        .orElse(null);
        return need(n, location);
    }

    private NeedSummaryResponse need(Necessity n, LocationResponse location) {
        return new NeedSummaryResponse(
                n.getId(),
                n.getInstitution().getId(),
                n.getInstitution().getLegalName(),
                n.getItemType().getName(),
                icons.resolve(n.getItemType().getCategory()),
                n.getDescription(),
                n.getItemType().getCategory(),
                n.getPriority(),
                n.getStatus(),
                n.getQuantityRequested(),
                Math.max(
                        0,
                        n.getQuantityRequested()
                                - matches.sumAllocatedByNecessity(
                                        n.getId(), MatchStatus.allocationConsumingStatuses())),
                location,
                null);
    }

    public DonationDeadlineResponse deadlines(DonationMatch m, Delivery d) {
        DonationStage stage = null;
        Instant deadline = null;
        if (m.getStatus() == MatchStatus.AWAITING_ACCEPTANCE) {
            stage = DonationStage.ACCEPTANCE;
            deadline = m.getAcceptanceDeadline();
        } else if (m.getStatus() == MatchStatus.AWAITING_SHIPMENT
                || m.getStatus() == MatchStatus.ACCEPTED) {
            stage = d == null ? DonationStage.CHOICE : DonationStage.CARRIER;
            deadline = d == null ? m.getDeliveryMethodDeadline() : d.getShippingDeadline();
        } else if (m.getStatus() == MatchStatus.AWAITING_DELIVERY) {
            stage = DonationStage.IN_PERSON;
            deadline = d == null ? null : d.getInPersonDeadline();
        }
        return new DonationDeadlineResponse(
                m.getAcceptanceDeadline(),
                m.getDeliveryMethodDeadline(),
                d == null ? null : d.getInPersonDeadline(),
                d == null ? null : d.getShippingDeadline(),
                stage,
                deadline);
    }

    public MatchSummaryResponse summary(DonationMatch m) {
        var d = deliveries.findByMatchId(m.getId()).orElse(null);
        var deadline = deadlines(m, d);
        var item = m.getDonation().getDonationPackage().getItem();
        return new MatchSummaryResponse(
                m.getId(),
                m.getProtocol(),
                item.getTitle(),
                icons.resolve(item.getItemType().getCategory()),
                m.getAllocatedQuantity(),
                m.getDonation().getDonor().getFullName(),
                m.getNecessity().getInstitution().getLegalName(),
                m.getStatus(),
                m.getCreatedAt(),
                d == null ? null : d.getMethod(),
                deadline.activeStage(),
                deadline.activeDeadline());
    }

    public MatchDetailResponse detail(DonationMatch m) {
        var d = deliveries.findByMatchId(m.getId()).orElse(null);
        var deadline = deadlines(m, d);
        boolean onTime =
                deadline.activeDeadline() != null
                        && Instant.now().isBefore(deadline.activeDeadline());
        boolean awaiting = m.getStatus() == MatchStatus.AWAITING_ACCEPTANCE;
        boolean choosing =
                d == null
                        && (m.getStatus() == MatchStatus.AWAITING_SHIPMENT
                                || m.getStatus() == MatchStatus.ACCEPTED);
        boolean cancellable =
                MatchStatus.activeStatuses().contains(m.getStatus())
                        && m.getStatus() != MatchStatus.IN_TRANSIT
                        && m.getStatus() != MatchStatus.AWAITING_NGO_CONFIRMATION;
        boolean personal = d != null && d.getMethod() == DeliveryMethod.IN_PERSON;
        boolean carrier = d != null && d.getMethod() == DeliveryMethod.CARRIER;
        boolean pending =
                d != null && d.getStatus() == DeliveryStatus.PENDING && d.getReportedAt() == null;
        boolean canReportDelivery =
                m.getStatus() == MatchStatus.AWAITING_DELIVERY && personal && pending && onTime;
        boolean canReportShipment =
                m.getStatus() == MatchStatus.AWAITING_SHIPMENT && carrier && pending && onTime;
        boolean canReceive =
                (m.getStatus() == MatchStatus.IN_TRANSIT
                                && carrier
                                && d.getStatus() == DeliveryStatus.IN_TRANSIT)
                        || (m.getStatus() == MatchStatus.AWAITING_NGO_CONFIRMATION
                                && personal
                                && d.getStatus() == DeliveryStatus.AWAITING_CONFIRMATION);
        var actions =
                new MatchActionsResponse(
                        awaiting && onTime,
                        awaiting && onTime,
                        cancellable && onTime,
                        choosing && onTime,
                        canReportDelivery,
                        canReportShipment,
                        canReceive);
        var delivery =
                d == null
                        ? null
                        : new DeliveryResponse(
                                d.getMethod(),
                                d.getStatus(),
                                d.getMethodConfirmedAt(),
                                d.getDeliveryAddress(),
                                d.getCarrierName(),
                                d.getTrackingCode(),
                                date(d.getShippedAt()),
                                date(d.getDeliveredAt()),
                                d.getReportedAt(),
                                d.getReceivedByName(),
                                d.getDeliveryNotes(),
                                d.getReceiptConfirmedAt(),
                                d.getReceiptConfirmedBy() == null
                                        ? null
                                        : d.getReceiptConfirmedBy().getId());
        var events =
                history.findAllByMatchIdOrderByChangedAtAscIdAsc(m.getId()).stream()
                        .map(
                                e ->
                                        new DonationHistoryEventResponse(
                                                e.getId(),
                                                e.getEventType(),
                                                e.getEventTitle() == null
                                                        ? e.getEventType().getLabel()
                                                        : e.getEventTitle(),
                                                e.getChangedAt(),
                                                e.getActorName() == null
                                                        ? "Sistema"
                                                        : e.getActorName(),
                                                e.getNotes()))
                        .toList();
        var recipient = recipient(m.getNecessity().getInstitution());
        return new MatchDetailResponse(
                m.getId(),
                m.getProtocol(),
                m.getStatus(),
                profile(m.getDonation().getDonor()),
                recipient,
                inventory(m.getDonation()),
                need(
                        m.getNecessity(),
                        recipient.address() == null ? null : recipient.address().location()),
                m.getAllocatedQuantity(),
                m.getProposalMessage(),
                m.getCreatedAt(),
                m.getAcceptedAt(),
                m.getRejectedAt(),
                m.getRejectionReason(),
                m.getCancelledAt(),
                m.getCancellationReason(),
                m.getExpiredStage(),
                m.getCompletedAt(),
                deadline,
                delivery,
                events,
                actions);
    }

    private LocalDate date(Instant at) {
        return at == null ? null : at.atZone(ZONE).toLocalDate();
    }
}

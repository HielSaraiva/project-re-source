package edu.br.resource.resourcesystem.presentation;

import edu.br.resource.resourcesystem.dto.response.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.service.match.MatchDtoMapper;

import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class MatchViewAssembler {
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(MatchDtoMapper.ZONE);
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm").withZone(MatchDtoMapper.ZONE);

    public static Map<String, Object> fields(Object... entries) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) result.put((String) entries[i], entries[i + 1]);
        return result;
    }

    public String date(Instant at) {
        return at == null ? "Não registrada" : DATE.format(at);
    }

    public String time(Instant at) {
        return at == null ? "Não registrado" : TIME.format(at);
    }

    private String text(String value) {
        return value == null || value.isBlank() ? "Não informado" : value;
    }

    private String units(long n) {
        return n + (n == 1 ? " unidade" : " unidades");
    }

    private String statusClass(MatchStatus status) {
        return switch (status) {
            case IN_TRANSIT -> "shipped";
            case COMPLETED -> "completed";
            case REJECTED -> "rejected";
            case CANCELLED -> "cancelled";
            default -> "waiting";
        };
    }

    public String donorPath(MatchStatus status, DeliveryMethod method) {
        return switch (status) {
            case COMPLETED -> "completed";
            case AWAITING_DELIVERY, AWAITING_NGO_CONFIRMATION -> "shipping/in-person";
            case IN_TRANSIT -> "shipping/carrier";
            case AWAITING_SHIPMENT, ACCEPTED ->
                    method == DeliveryMethod.CARRIER ? "shipping/carrier" : "shipping";
            case CANCELLED -> "status";
            default -> "status";
        };
    }

    private String deadline(DonationStage stage, Instant due) {
        if (due == null) return "";
        return (stage == DonationStage.ACCEPTANCE
                        ? "Aceite até "
                        : stage == DonationStage.CHOICE
                                ? "Escolha até "
                                : stage == DonationStage.IN_PERSON
                                        ? "Entrega até "
                                        : "Postagem até ")
                + time(due);
    }

    public Map<String, Object> summary(MatchSummaryResponse d) {
        return fields(
                "id",
                d.protocol(),
                "protocol",
                d.protocol(),
                "item",
                d.item(),
                "quantity",
                d.quantity(),
                "icon",
                d.icon(),
                "donor",
                d.donor(),
                "organization",
                d.organization(),
                "recipient",
                d.organization(),
                "status",
                d.status().getValue(),
                "statusLabel",
                d.status().getLabel(),
                "statusClass",
                statusClass(d.status()),
                "createdAt",
                d.createdAt(),
                "createdLabel",
                time(d.createdAt()),
                "acceptanceDeadline",
                d.deadlineStage() == DonationStage.ACCEPTANCE ? d.deadline() : null,
                "deadlineLabel",
                d.deadlineStage() == DonationStage.ACCEPTANCE ? time(d.deadline()) : "",
                "currentDeadlineLabel",
                deadline(d.deadlineStage(), d.deadline()),
                "detailsPath",
                "/donor/donation/"
                        + donorPath(d.status(), d.deliveryMethod())
                        + "?protocol="
                        + d.protocol());
    }

    public Map<String, Object> need(NeedSummaryResponse n) {
        return fields(
                "id",
                n.id(),
                "needId",
                n.id(),
                "organization",
                n.organization(),
                "institution",
                n.organization(),
                "item",
                n.item(),
                "icon",
                n.icon(),
                "description",
                text(n.description()),
                "category",
                n.category().getLabel(),
                "priority",
                n.priority().getLabel(),
                "priorityClass",
                n.priority().getValue(),
                "requestedQuantity",
                n.remainingQuantity(),
                "quantity",
                units(n.remainingQuantity()),
                "city",
                n.location() == null
                        ? "Localização não cadastrada"
                        : n.location().city() + ", " + n.location().state(),
                "distance",
                n.distanceKm() == null ? "distância não disponível" : n.distanceKm() + " km");
    }

    public Map<String, Object> inventory(InventoryDonationResponse d) {
        return fields(
                "id",
                d.id(),
                "item",
                d.item(),
                "icon",
                d.icon(),
                "quantity",
                d.availableQuantity(),
                "condition",
                d.condition().getLabel(),
                "description",
                text(d.description()),
                "status",
                d.availableQuantity() > 0 ? "registered" : d.status().getValue());
    }

    public Map<String, Object> event(DonationHistoryEventResponse e) {
        return fields(
                "title",
                e.title(),
                "at",
                e.at(),
                "dateLabel",
                time(e.at()),
                "actor",
                e.actor(),
                "description",
                text(e.description()));
    }

    private DonationDetailResponse detail(String icon, String label, String value) {
        return new DonationDetailResponse(icon, label, text(value));
    }

    public String address(AddressResponse a) {
        return a == null ? "Endereço não cadastrado" : a.formatted();
    }

    private List<DonationDetailResponse> recipient(RecipientResponse r) {
        return List.of(
                detail("home.svg", "Destinatário", r.name()),
                detail("file-text.svg", "CNPJ", r.cnpj()),
                detail("map.svg", "Endereço de recebimento", address(r.address())),
                detail(
                        "user.svg",
                        "Contato",
                        r.phone() == null ? r.email() : r.phone() + " · " + r.email()));
    }

    private String cancellationReason(MatchDetailResponse match) {
        if (match.expiredStage() == null) return match.cancellationReason();
        String action =
                switch (match.expiredStage()) {
                    case ACCEPTANCE -> "a ONG aceitar a proposta";
                    case CHOICE -> "escolher a modalidade de entrega";
                    case IN_PERSON -> "confirmar a entrega presencial";
                    case CARRIER -> "confirmar a postagem pelos Correios";
                };
        return "O prazo de 7 dias para "
                + action
                + " terminou. A doação foi cancelada automaticamente e a reserva foi liberada.";
    }

    private String institutionStatusDescription(MatchDetailResponse match) {
        return switch (match.status()) {
            case PROPOSED -> "A proposta foi criada e ainda aguarda o envio para análise da ONG.";
            case AWAITING_ACCEPTANCE ->
                    "Analise a proposta antes do prazo de aceite. A ONG tem 7 dias desde o envio"
                        + " para aceitar ou recusar. Sem aceite, a doação será cancelada"
                        + " automaticamente e a reserva será liberada.";
            case ACCEPTED, AWAITING_SHIPMENT ->
                    match.delivery() == null
                            ? "A proposta foi aceita. O doador tem 7 dias desde o aceite para"
                                  + " escolher entre entrega presencial e Correios. Sem escolha no"
                                  + " prazo, a doação será cancelada automaticamente e a reserva"
                                  + " será liberada."
                            : "O doador escolheu enviar pelos Correios e tem 7 dias desde a escolha"
                                  + " para informar a postagem e o rastreamento. Sem confirmação no"
                                  + " prazo, a doação será cancelada automaticamente e a reserva"
                                  + " será liberada.";
            case AWAITING_DELIVERY ->
                    "O doador escolheu a entrega presencial e tem 7 dias desde a escolha para"
                        + " informar a entrega à ONG. Sem confirmação no prazo, a doação será"
                        + " cancelada automaticamente e a reserva será liberada.";
            case AWAITING_NGO_CONFIRMATION ->
                    "O doador informou a entrega presencial. Confira os itens e a quantidade e"
                        + " confirme o recebimento para concluir a doação.";
            case IN_TRANSIT ->
                    "O doador informou a postagem pelos Correios. Acompanhe o rastreamento e"
                        + " confirme o recebimento somente após receber e conferir todos os itens.";
            case COMPLETED ->
                    "A ONG confirmou o recebimento em "
                            + time(match.completedAt())
                            + ". A doação está concluída. Os dados da entrega e os eventos"
                            + " permanecem disponíveis no histórico.";
            case REJECTED ->
                    "A ONG recusou a proposta. A data e o motivo estão registrados no histórico. Os"
                        + " itens e a quantidade reservada da necessidade foram liberados; esta"
                        + " proposta não pode ser reaberta.";
            case CANCELLED ->
                    match.expiredStage() != null
                            ? cancellationReason(match)
                            : "A doação foi cancelada pelo doador. Os itens e a quantidade"
                                  + " reservada da necessidade foram liberados. O cancelamento não"
                                  + " pode ser desfeito; uma nova doação exige outra proposta.";
        };
    }

    private Instant expiredDeadline(MatchDetailResponse match) {
        if (match.expiredStage() == null) return null;
        return switch (match.expiredStage()) {
            case ACCEPTANCE -> match.deadlines().acceptance();
            case CHOICE -> match.deadlines().methodSelection();
            case IN_PERSON -> match.deadlines().inPersonDelivery();
            case CARRIER -> match.deadlines().carrierShipment();
        };
    }

    public Map<String, Object> detail(MatchDetailResponse d) {
        var delivery = d.delivery();
        var deadlines = d.deadlines();
        var response =
                fields(
                        "donorName",
                        d.donor().name(),
                        "ongName",
                        d.recipient().name(),
                        "ongRole",
                        "Administrador",
                        "protocol",
                        d.protocol(),
                        "match",
                        d,
                        "rejected",
                        d.status() == MatchStatus.REJECTED,
                        "cancelled",
                        d.status() == MatchStatus.CANCELLED,
                        "rejectedAt",
                        d.rejectedAt(),
                        "rejectedAtLabel",
                        time(d.rejectedAt()),
                        "rejectionReason",
                        d.rejectionReason(),
                        "cancelledAtLabel",
                        time(d.cancelledAt()),
                        "cancellationReason",
                        cancellationReason(d),
                        "statusLabel",
                        d.status().getLabel(),
                        "statusClass",
                        statusClass(d.status()),
                        "actions",
                        d.actions(),
                        "acceptanceDeadline",
                        deadlines.acceptance(),
                        "choiceDeadline",
                        deadlines.methodSelection(),
                        "expiredStage",
                        d.expiredStage() == null ? null : d.expiredStage().getValue(),
                        "expiredStageLabel",
                        d.expiredStage() == null ? null : d.expiredStage().getLabel(),
                        "expiredDeadlineLabel",
                        time(expiredDeadline(d)),
                        "methodConfirmedAt",
                        delivery == null ? null : delivery.methodConfirmedAt(),
                        "acceptedOnLabel",
                        date(d.acceptedAt()),
                        "deliveryDeadlineLabel",
                        time(deadlines.activeDeadline()),
                        "delivery",
                        delivery,
                        "donationHistory",
                        d.history().stream().map(this::event).toList());
        response.put(
                "donation",
                fields(
                        "protocol",
                        d.protocol(),
                        "item",
                        d.allocatedQuantity() + "× " + d.donation().item(),
                        "organization",
                        d.recipient().name(),
                        "category",
                        d.donation().category().getLabel(),
                        "condition",
                        d.donation().condition().getLabel(),
                        "icon",
                        d.donation().icon()));
        response.put(
                "donationDetails",
                List.of(
                        detail(
                                "gift.svg",
                                "Item doado",
                                d.allocatedQuantity() + "× " + d.donation().item()),
                        detail("tag.svg", "Categoria", d.donation().category().getLabel()),
                        detail("check-circle.svg", "Condição", d.donation().condition().getLabel()),
                        detail("calendar.svg", "Data da proposta", date(d.createdAt()))));
        List<DonationDetailResponse> organization = new ArrayList<>();
        organization.add(detail("user.svg", "Destinatário", d.recipient().name()));
        organization.add(detail("file-text.svg", "CNPJ", d.recipient().cnpj()));
        organization.add(detail("mail.svg", "Contato", d.recipient().email()));
        if (d.status() == MatchStatus.AWAITING_ACCEPTANCE)
            organization.add(
                    detail("clock.svg", "Prazo para Aceite", time(deadlines.acceptance())));
        response.put("organizationDetails", organization);
        response.put("recipientDetails", recipient(d.recipient()));
        List<DonationDetailResponse> receiving = new ArrayList<>(recipient(d.recipient()));
        for (var h : d.recipient().receivingHours())
            receiving.add(
                    detail(
                            "clock.svg",
                            "Horário de recebimento",
                            h.days().stream()
                                            .map(
                                                    day ->
                                                            day.getDisplayName(
                                                                    java.time.format.TextStyle
                                                                            .SHORT,
                                                                    Locale.forLanguageTag("pt-BR")))
                                            .reduce((a, b) -> a + ", " + b)
                                            .orElse("")
                                    + ": "
                                    + h.openingHour()
                                    + "–"
                                    + h.closingHour()
                                    + " · "
                                    + h.responsibleName()));
        response.put("receivingDetails", receiving);
        response.put(
                "inPerson",
                fields(
                        "icon",
                        "map-pin.svg",
                        "title",
                        "Entrega presencial",
                        "description",
                        "Leve a doação diretamente ao endereço de recebimento da ONG.",
                        "badge",
                        "Entrega presencial",
                        "action",
                        "Entregar presencialmente",
                        "details",
                        receiving));
        response.put(
                "mail",
                fields(
                        "icon",
                        "package.svg",
                        "title",
                        "Envio via Correios",
                        "description",
                        "Confira o destinatário, poste a doação e informe o rastreamento. O frete é"
                            + " de responsabilidade do doador.",
                        "badge",
                        "Envio pelos Correios",
                        "action",
                        "Enviar pelos Correios",
                        "details",
                        recipient(d.recipient())));
        Instant method = delivery == null ? null : delivery.methodConfirmedAt();
        LocalDate today = LocalDate.now(MatchDtoMapper.ZONE);
        LocalDate limit =
                deadlines.activeDeadline() == null
                        ? today
                        : deadlines.activeDeadline().atZone(MatchDtoMapper.ZONE).toLocalDate();
        response.put("today", today);
        response.put("deliveryDeadline", limit);
        response.put(
                "methodConfirmedOn",
                method == null ? today : method.atZone(MatchDtoMapper.ZONE).toLocalDate());
        response.put("methodConfirmedOnLabel", date(method));
        List<DonationDetailResponse> completed = new ArrayList<>();
        if (delivery != null) {
            completed.add(detail("home.svg", "Tipo de entrega", delivery.method().getLabel()));
            if (delivery.postedOn() != null)
                completed.add(
                        detail(
                                "calendar.svg",
                                "Data da postagem",
                                delivery.postedOn()
                                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            if (delivery.trackingCode() != null)
                completed.add(detail("file-text.svg", "Rastreamento", delivery.trackingCode()));
            if (delivery.deliveredOn() != null)
                completed.add(
                        detail(
                                "calendar.svg",
                                "Data de recebimento",
                                delivery.deliveredOn()
                                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            completed.add(
                    detail(
                            "check-circle.svg",
                            "Recebimento confirmado pela ONG",
                            time(delivery.receiptConfirmedAt())));
            if (delivery.receivedByName() != null)
                completed.add(detail("user.svg", "Recebido por", delivery.receivedByName()));
            if (delivery.notes() != null)
                completed.add(detail("file-text.svg", "Observações", delivery.notes()));
        }
        response.put("deliveryDetails", completed);
        var intention =
                fields(
                        "id",
                        d.protocol(),
                        "item",
                        d.donation().item(),
                        "icon",
                        d.donation().icon(),
                        "quantity",
                        d.allocatedQuantity(),
                        "donor",
                        d.donor().name(),
                        "category",
                        d.donation().category().getLabel(),
                        "statusDescription",
                        institutionStatusDescription(d),
                        "condition",
                        d.donation().condition().getLabel(),
                        "need",
                        d.need().item(),
                        "description",
                        text(d.donation().description()),
                        "message",
                        text(d.proposalMessage()),
                        "status",
                        d.status().getValue(),
                        "statusClass",
                        statusClass(d.status()),
                        "statusLabel",
                        d.status().getLabel(),
                        "createdAt",
                        d.createdAt(),
                        "createdLabel",
                        time(d.createdAt()),
                        "acceptanceDeadline",
                        deadlines.acceptance(),
                        "choiceDeadline",
                        deadlines.methodSelection(),
                        "inPersonDeadline",
                        deadlines.inPersonDelivery(),
                        "deliveryMethod",
                        delivery == null ? null : delivery.method().getValue(),
                        "postedOnLabel",
                        delivery == null || delivery.postedOn() == null
                                ? "Não informada"
                                : delivery.postedOn()
                                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        "trackingCode",
                        delivery == null ? null : delivery.trackingCode(),
                        "currentDeadlineLabel",
                        deadline(deadlines.activeStage(), deadlines.activeDeadline()),
                        "history",
                        d.history().stream().map(this::event).toList());
        response.put("intention", intention);
        return response;
    }
}

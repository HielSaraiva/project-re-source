package edu.br.resource.resourcesystem.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MatchEventType implements DatabaseEnum {
    STATUS_CHANGED("status_changed", "Status atualizado"),
    PROPOSAL_SUBMITTED("proposal_submitted", "Proposta enviada"),
    PROPOSAL_ACCEPTED("proposal_accepted", "Proposta aceita"),
    PROPOSAL_REJECTED("proposal_rejected", "Proposta recusada"),
    DELIVERY_METHOD_SELECTED("delivery_method_selected", "Modalidade escolhida"),
    DELIVERY_REPORTED("delivery_reported", "Entrega informada"),
    SHIPMENT_REPORTED("shipment_reported", "Postagem informada"),
    RECEIPT_CONFIRMED("receipt_confirmed", "Recebimento confirmado"),
    DONATION_CANCELLED("donation_cancelled", "Doação cancelada");

    @EnumeratedValue
    private final String value;
    private final String label;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MatchEventType fromValue(String value) {
        return EnumValues.fromValue(MatchEventType.class, value);
    }
}

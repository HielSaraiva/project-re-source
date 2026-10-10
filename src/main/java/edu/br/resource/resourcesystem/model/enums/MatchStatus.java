package edu.br.resource.resourcesystem.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MatchStatus implements DatabaseEnum {
    PROPOSED("proposed", "Proposta criada"),
    AWAITING_ACCEPTANCE("awaiting_acceptance", "Aguardando aceite"),
    ACCEPTED("accepted", "Aceito"),
    REJECTED("rejected", "Recusado"),
    AWAITING_SHIPMENT("awaiting_shipment", "Aguardando envio"),
    IN_TRANSIT("in_transit", "Enviado"),
    COMPLETED("completed", "Concluído"),
    CANCELLED("cancelled", "Doação cancelada"),
    AWAITING_DELIVERY("awaiting_delivery", "Aguardando entrega"),
    AWAITING_NGO_CONFIRMATION("awaiting_ngo_confirmation", "Aguardando confirmação da ONG");

    @EnumeratedValue private final String value;
    private final String label;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Set<MatchStatus> allocationConsumingStatuses() {
        return Collections.unmodifiableSet(EnumSet.complementOf(EnumSet.of(REJECTED, CANCELLED)));
    }

    public static Set<MatchStatus> activeStatuses() {
        return Collections.unmodifiableSet(
                EnumSet.complementOf(EnumSet.of(REJECTED, CANCELLED, COMPLETED)));
    }

    @JsonCreator
    public static MatchStatus fromValue(String value) {
        return EnumValues.fromValue(MatchStatus.class, value);
    }
}

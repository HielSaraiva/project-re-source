package edu.br.resource.resourcesystem.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DonationStatus implements DatabaseEnum {
    REGISTERED("registered", "Registrado"),
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

    @JsonCreator
    public static DonationStatus fromValue(String value) {
        return EnumValues.fromValue(DonationStatus.class, value);
    }
}

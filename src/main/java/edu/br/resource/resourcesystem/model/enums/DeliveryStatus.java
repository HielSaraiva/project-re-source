package edu.br.resource.resourcesystem.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryStatus implements DatabaseEnum {
    PENDING("pending", "Pendente"),
    SCHEDULED("scheduled", "Agendada"),
    IN_TRANSIT("in_transit", "Enviado"),
    DELIVERED("delivered", "Recebida"),
    CANCELLED("cancelled", "Cancelada"),
    AWAITING_CONFIRMATION("awaiting_confirmation", "Aguardando confirmação da ONG");

    @EnumeratedValue private final String value;
    private final String label;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DeliveryStatus fromValue(String value) {
        return EnumValues.fromValue(DeliveryStatus.class, value);
    }
}

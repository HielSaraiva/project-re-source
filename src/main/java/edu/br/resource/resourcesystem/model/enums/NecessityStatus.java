package edu.br.resource.resourcesystem.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NecessityStatus implements DatabaseEnum {
    ACTIVE("active", "Ativa"),
    FULFILLED("fulfilled", "Atendida"),
    CANCELLED("cancelled", "Cancelada");

    @EnumeratedValue
    private final String value;
    private final String label;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static NecessityStatus fromValue(String value) {
        return EnumValues.fromValue(NecessityStatus.class, value);
    }
}

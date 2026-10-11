package edu.br.resource.resourcesystem.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountStatus implements DatabaseEnum {
    ACTIVE("active", "Ativo"),
    PENDING("pending", "Pendente"),
    BLOCKED("blocked", "Bloqueado");

    @EnumeratedValue private final String value;
    private final String label;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AccountStatus fromValue(String value) {
        return EnumValues.fromValue(AccountStatus.class, value);
    }
}

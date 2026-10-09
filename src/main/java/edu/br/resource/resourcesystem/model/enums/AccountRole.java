package edu.br.resource.resourcesystem.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountRole implements DatabaseEnum {
    DONOR("donor", "Doador"),
    ADMINISTRATOR("administrator", "Administrador");

    @EnumeratedValue
    private final String value;
    private final String label;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AccountRole fromValue(String value) {
        return EnumValues.fromValue(AccountRole.class, value);
    }
}

package edu.br.resource.resourcesystem.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemCondition implements DatabaseEnum {
    NEW("new", "Novo"),
    GOOD("good", "Usado (Bom estado)"),
    USED("used", "Com marcas de uso"),
    OTHER("other", "Outro");

    @EnumeratedValue private final String value;
    private final String label;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ItemCondition fromValue(String value) {
        return EnumValues.fromValue(ItemCondition.class, value);
    }
}

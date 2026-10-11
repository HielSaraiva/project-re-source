package edu.br.resource.resourcesystem.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InstitutionSector implements DatabaseEnum {
    EDUCATION("education", "Educação"),
    HEALTH("health", "Saúde"),
    SOCIAL_ASSISTANCE("social_assistance", "Assistência social"),
    ENVIRONMENT("environment", "Meio ambiente"),
    OTHER("other", "Outros");

    @EnumeratedValue private final String value;
    private final String label;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static InstitutionSector fromValue(String value) {
        return EnumValues.fromValue(InstitutionSector.class, value);
    }
}

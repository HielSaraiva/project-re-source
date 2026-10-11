package edu.br.resource.resourcesystem.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NecessityPriority implements DatabaseEnum {
    HIGH("high", "Alta prioridade"),
    MEDIUM("medium", "Média prioridade"),
    LOW("low", "Baixa prioridade");

    @EnumeratedValue private final String value;
    private final String label;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static NecessityPriority fromValue(String value) {
        return EnumValues.fromValue(NecessityPriority.class, value);
    }
}

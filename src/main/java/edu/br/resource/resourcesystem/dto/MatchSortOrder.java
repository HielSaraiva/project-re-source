package edu.br.resource.resourcesystem.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import edu.br.resource.resourcesystem.model.enums.DatabaseEnum;
import edu.br.resource.resourcesystem.model.enums.EnumValues;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MatchSortOrder implements DatabaseEnum {
    RECENT("recent"), DEADLINE("deadline");

    @JsonValue
    private final String value;

    @JsonCreator
    public static MatchSortOrder fromValue(String value) {
        return EnumValues.fromValue(MatchSortOrder.class, value);
    }
}

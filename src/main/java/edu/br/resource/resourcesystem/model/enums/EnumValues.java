package edu.br.resource.resourcesystem.model.enums;

import java.util.Arrays;

public final class EnumValues {
    private EnumValues() {}

    public static <T extends Enum<T> & DatabaseEnum> T fromValue(Class<T> type, String value) {
        return Arrays.stream(type.getEnumConstants())
                .filter(constant -> constant.getValue().equals(value))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Valor inválido para "
                                                + type.getSimpleName()
                                                + ": "
                                                + value));
    }
}

package edu.br.resource.resourcesystem.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DonationStage implements DatabaseEnum {
    ACCEPTANCE("acceptance", "Aceite da ONG"),
    CHOICE("choice", "Escolha da modalidade"),
    IN_PERSON("in_person", "Entrega presencial"),
    CARRIER("carrier", "Postagem pelos Correios");

    @EnumeratedValue
    private final String value;
    private final String label;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DonationStage fromValue(String value) {
        return EnumValues.fromValue(DonationStage.class, value);
    }
}

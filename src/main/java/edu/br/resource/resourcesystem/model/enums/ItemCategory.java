package edu.br.resource.resourcesystem.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemCategory implements DatabaseEnum {
    FOOD("food", "Alimentação"),
    CLOTHING("clothing", "Vestuário"),
    HOUSEHOLD("household", "Utilidades domésticas"),
    TOY("toy", "Brinquedos"),
    EDUCATION("education", "Educação"),
    HYGIENE("hygiene", "Higiene"),
    OTHER("other", "Outros"),
    ELECTRONICS("electronics", "Eletrônicos"),
    MOBILITY("mobility", "Saúde & Mobilidade"),
    FURNITURE("furniture", "Móveis"),
    BEDDING("bedding", "Vestuário & Cama");

    @EnumeratedValue
    private final String value;
    private final String label;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ItemCategory fromValue(String value) {
        return EnumValues.fromValue(ItemCategory.class, value);
    }
}

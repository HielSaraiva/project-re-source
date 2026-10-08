package edu.br.resource.resourcesystem.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InstitutionStatus implements DatabaseEnum {
    PENDING_APPROVAL("pending_approval", "Aguardando aprovação"),
    APPROVED("approved", "Aprovada"),
    REJECTED("rejected", "Recusada"),
    BLOCKED("blocked", "Bloqueada");

    @EnumeratedValue
    private final String value;
    private final String label;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static InstitutionStatus fromValue(String value) {
        return EnumValues.fromValue(InstitutionStatus.class, value);
    }
}

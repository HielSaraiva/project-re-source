package edu.br.resource.resourcesystem.model.enums;

import jakarta.persistence.EnumeratedValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentStatus implements DatabaseEnum {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    @EnumeratedValue private final String value;

    public String getLabel() {
        return value;
    }
}

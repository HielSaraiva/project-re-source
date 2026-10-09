package edu.br.resource.resourcesystem.model.enums;

import jakarta.persistence.EnumeratedValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentType implements DatabaseEnum {
    IDENTITY_DOCUMENT("identity_document"),
    ORGANIZATION_DOCUMENT("organization_document");

    @EnumeratedValue
    private final String value;
    public String getLabel() { return value; }
}

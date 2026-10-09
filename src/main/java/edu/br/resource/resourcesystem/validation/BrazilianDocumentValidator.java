package edu.br.resource.resourcesystem.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BrazilianDocumentValidator implements ConstraintValidator<BrazilianDocument, String> {
    private BrazilianDocument.Type type;
    @Override
    public void initialize(BrazilianDocument annotation) { type = annotation.value(); }
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        return type == BrazilianDocument.Type.CNPJ ? BrazilianDocuments.validCnpj(value) : BrazilianDocuments.validCpf(value);
    }
}

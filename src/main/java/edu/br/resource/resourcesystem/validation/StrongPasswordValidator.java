package edu.br.resource.resourcesystem.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true; // @NotBlank owns required-field feedback.
        if (!PasswordPolicy.withinByteLimit(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(PasswordPolicy.BYTE_LIMIT_MESSAGE).addConstraintViolation();
            return false;
        }
        return PasswordPolicy.validFormat(value);
    }
}

package edu.br.resource.resourcesystem.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

public class MatchingPasswordsValidator implements ConstraintValidator<MatchingPasswords, PasswordConfirmation> {
    @Override
    public boolean isValid(PasswordConfirmation value, ConstraintValidatorContext context) {
        if (value == null || Objects.equals(value.getPassword(), value.getPasswordConfirmation())) return true;
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("passwordConfirmation").addConstraintViolation();
        return false;
    }
}

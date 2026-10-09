package edu.br.resource.resourcesystem.validation;

import edu.br.resource.resourcesystem.dto.request.RegistrationCredentials;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

public class MatchingPasswordsValidator implements ConstraintValidator<MatchingPasswords, RegistrationCredentials> {
    @Override
    public boolean isValid(RegistrationCredentials value, ConstraintValidatorContext context) {
        if (value == null || Objects.equals(value.getPassword(), value.getPasswordConfirmation())) return true;
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("passwordConfirmation").addConstraintViolation();
        return false;
    }
}

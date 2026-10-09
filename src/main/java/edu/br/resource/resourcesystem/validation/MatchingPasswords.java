package edu.br.resource.resourcesystem.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Constraint(validatedBy = MatchingPasswordsValidator.class)
public @interface MatchingPasswords {
    String message() default "As senhas devem ser iguais.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

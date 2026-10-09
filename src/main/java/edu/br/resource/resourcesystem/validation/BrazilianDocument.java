package edu.br.resource.resourcesystem.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BrazilianDocumentValidator.class)
public @interface BrazilianDocument {
    Type value();
    String message() default "Informe um documento válido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    enum Type { CNPJ, CPF }
}

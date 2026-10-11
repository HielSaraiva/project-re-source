package edu.br.resource.resourcesystem.dto.request;

import edu.br.resource.resourcesystem.model.enums.DeliveryMethod;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record SelectDeliveryMethodRequest(
        @NotNull DeliveryMethod method, @NotNull @AssertTrue Boolean confirmed) {

    @AssertTrue(message = "Escolha entrega presencial ou envio pelos Correios.")
    public boolean isSupportedMethod() {
        return method == null
                || method == DeliveryMethod.IN_PERSON
                || method == DeliveryMethod.CARRIER;
    }
}

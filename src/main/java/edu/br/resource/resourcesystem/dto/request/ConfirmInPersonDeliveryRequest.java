package edu.br.resource.resourcesystem.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ConfirmInPersonDeliveryRequest(
        @NotNull @PastOrPresent LocalDate date,
        @NotBlank @Size(max = 120) String recipient,
        @Size(max = 1000) String notes,
        @NotNull @AssertTrue Boolean delivered) {
}

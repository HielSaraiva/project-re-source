package edu.br.resource.resourcesystem.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ConfirmCarrierShipmentRequest(
        @NotNull @PastOrPresent LocalDate date,
        @NotBlank @Pattern(regexp = "(?i)[A-Z]{2}[0-9]{9}[A-Z]{2}") String trackingCode,
        @Size(max = 1000) String notes,
        @NotNull @AssertTrue Boolean posted) {
}

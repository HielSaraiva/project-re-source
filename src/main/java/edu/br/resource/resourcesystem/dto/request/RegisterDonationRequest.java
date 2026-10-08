package edu.br.resource.resourcesystem.dto.request;

import edu.br.resource.resourcesystem.model.enums.ItemCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonAlias;
import org.springframework.web.bind.annotation.BindParam;

public record RegisterDonationRequest(
        @BindParam("need") @JsonAlias("need") @NotNull @Positive Integer necessityId,
        @BindParam("item") @JsonAlias("item") @NotBlank @Size(max = 120) String title,
        @NotNull @Positive Integer quantity,
        @NotNull ItemCondition condition,
        @Size(max = 1000) String description) {
}

package edu.br.resource.resourcesystem.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonAlias;
import org.springframework.web.bind.annotation.BindParam;

public record SubmitProposalRequest(
        @BindParam("need") @JsonAlias("need") @NotNull @Positive Integer necessityId,
        @BindParam("donation") @JsonAlias("donation") @NotNull @Positive Integer donationId,
        @NotNull @Positive Integer quantity,
        @BindParam("description") @JsonAlias("description") @Size(max = 1000) String message) {}

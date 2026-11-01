package edu.br.resource.resourcesystem.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import org.springframework.web.bind.annotation.BindParam;

public record SubmitProposalRequest(
        @BindParam("need")
                @JsonAlias("need")
                @tools.jackson.databind.annotation.JsonDeserialize(
                        using =
                                edu.br.resource.resourcesystem.validation.WholeQuantityDeserializer
                                        .class)
                @NotNull
                @Positive
                Integer necessityId,
        @BindParam("donation")
                @JsonAlias("donation")
                @tools.jackson.databind.annotation.JsonDeserialize(
                        using =
                                edu.br.resource.resourcesystem.validation.WholeQuantityDeserializer
                                        .class)
                @NotNull
                @Positive
                Integer donationId,
        @tools.jackson.databind.annotation.JsonDeserialize(
                        using =
                                edu.br.resource.resourcesystem.validation.WholeQuantityDeserializer
                                        .class)
                @NotNull
                @Positive
                Integer quantity,
        @BindParam("description") @JsonAlias("description") @Size(max = 1000) String message) {}

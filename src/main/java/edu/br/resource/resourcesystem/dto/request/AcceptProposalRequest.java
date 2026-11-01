package edu.br.resource.resourcesystem.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record AcceptProposalRequest(@NotNull @AssertTrue Boolean confirmed) {}

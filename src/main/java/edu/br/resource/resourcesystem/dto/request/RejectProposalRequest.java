package edu.br.resource.resourcesystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectProposalRequest(@NotBlank @Size(max = 1000) String reason) {}

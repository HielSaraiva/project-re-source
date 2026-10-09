package edu.br.resource.resourcesystem.dto.response;

public record ProfileResponse(
        Integer id,
        String name,
        String role,
        String initials) {
}

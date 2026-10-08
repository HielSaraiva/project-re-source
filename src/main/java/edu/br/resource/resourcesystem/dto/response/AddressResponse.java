package edu.br.resource.resourcesystem.dto.response;



public record AddressResponse(
        String postalCode,
        String street,
        String number,
        String complement,
        String district,
        LocationResponse location) {
}

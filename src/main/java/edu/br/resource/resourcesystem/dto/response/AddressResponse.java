package edu.br.resource.resourcesystem.dto.response;

public record AddressResponse(
        String postalCode,
        String street,
        String number,
        String complement,
        String district,
        LocationResponse location) {
    public String formatted() {
        return street
                + ", "
                + number
                + (complement == null ? "" : " — " + complement)
                + ", "
                + district
                + ", "
                + location.city()
                + "/"
                + location.state()
                + ", CEP "
                + postalCode;
    }
}

package edu.br.resource.resourcesystem.dto.response;

import java.util.List;

public record RecipientResponse(
        Integer id,
        String name,
        String cnpj,
        String email,
        String phone,
        AddressResponse address,
        List<ReceivingHoursResponse> receivingHours) {

    public RecipientResponse {
        receivingHours = List.copyOf(receivingHours);
    }
}

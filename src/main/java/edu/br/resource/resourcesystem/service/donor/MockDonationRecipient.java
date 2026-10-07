package edu.br.resource.resourcesystem.service.donor;

import edu.br.resource.resourcesystem.model.view.DonationDetail;

import java.util.List;

/** Dados de recebimento que serão fornecidos pelo cadastro da ONG. */
record MockDonationRecipient(String name, String cnpj, String street, String number,
                             String complement, String neighborhood, String city, String state,
                             String postalCode, String contactName, String phone) {
    static MockDonationRecipient institutoEsperanca() {
        return new MockDonationRecipient("Instituto Esperança", "00.000.000/0001-00",
                "Rua das Flores", "123", "Recepção no térreo", "Centro", "Fortaleza", "CE",
                "60000-000", "Maria Silva", "(85) 3333-1234");
    }

    String address() {
        return street + ", " + number + " — " + neighborhood + ", " + city + "/" + state;
    }

    List<DonationDetail> postalDetails() {
        return List.of(
                new DonationDetail("home.svg", "Destinatário", name),
                new DonationDetail("file-text.svg", "CNPJ", cnpj),
                new DonationDetail("map.svg", "Endereço de recebimento", street + ", " + number),
                new DonationDetail("map-pin.svg", "Complemento", complement),
                new DonationDetail("map.svg", "Bairro", neighborhood),
                new DonationDetail("map.svg", "Cidade / UF", city + " / " + state),
                new DonationDetail("truck.svg", "CEP", postalCode),
                new DonationDetail("user.svg", "Responsável pelo recebimento", contactName),
                new DonationDetail("user.svg", "Telefone da ONG", phone));
    }
}

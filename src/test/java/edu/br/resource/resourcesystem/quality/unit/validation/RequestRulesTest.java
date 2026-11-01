package edu.br.resource.resourcesystem.quality.unit.validation;

import static org.assertj.core.api.Assertions.*;

import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.validation.*;

import jakarta.validation.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.*;

@DisplayName("RN-003/005/007/011/012/016/017/018 — contratos e limites de entrada")
class RequestRulesTest {
    static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    static Validator validator = factory.getValidator();

    @AfterAll
    static void close() {
        factory.close();
    }

    static LocalDate past() {
        return LocalDate.of(2020, 1, 1);
    }

    static Stream<Arguments> invalidRequests() {
        var list = new ArrayList<Arguments>();
        for (Integer n : new Integer[] {null, 0, -1}) {
            list.add(
                    Arguments.of(
                            "proposta sem necessidade válida",
                            new SubmitProposalRequest(n, 1, 1, null),
                            "necessityId"));
            list.add(
                    Arguments.of(
                            "proposta sem inventário próprio identificável",
                            new SubmitProposalRequest(1, n, 1, null),
                            "donationId"));
            list.add(
                    Arguments.of(
                            "proposta sem quantidade positiva",
                            new SubmitProposalRequest(1, 1, n, null),
                            "quantity"));
            list.add(
                    Arguments.of(
                            "cadastro sem quantidade positiva",
                            new RegisterDonationRequest(
                                    1, "Item", n, ItemCondition.GOOD, null, UUID.randomUUID()),
                            "quantity"));
        }
        list.add(
                Arguments.of(
                        "mensagem acima de 1000",
                        new SubmitProposalRequest(1, 1, 1, "x".repeat(1001)),
                        "message"));
        for (String title : new String[] {null, "", " ", "x".repeat(121)})
            list.add(
                    Arguments.of(
                            "nome inválido",
                            new RegisterDonationRequest(
                                    1, title, 1, ItemCondition.GOOD, null, UUID.randomUUID()),
                            "title"));
        list.add(
                Arguments.of(
                        "sem condição",
                        new RegisterDonationRequest(1, "Item", 1, null, null, UUID.randomUUID()),
                        "condition"));
        list.add(
                Arguments.of(
                        "sem chave de tentativa",
                        new RegisterDonationRequest(1, "Item", 1, ItemCondition.GOOD, null, null),
                        "submissionKey"));
        list.add(
                Arguments.of(
                        "descrição longa",
                        new RegisterDonationRequest(
                                1,
                                "Item",
                                1,
                                ItemCondition.GOOD,
                                "x".repeat(1001),
                                UUID.randomUUID()),
                        "description"));
        for (Boolean confirmation : new Boolean[] {null, false}) {
            list.add(
                    Arguments.of(
                            "aceite não confirmado",
                            new AcceptProposalRequest(confirmation),
                            "confirmed"));
            list.add(
                    Arguments.of(
                            "cancelamento não confirmado",
                            new CancelDonationRequest(confirmation),
                            "confirmed"));
            list.add(
                    Arguments.of(
                            "recebimento não confirmado",
                            new ConfirmReceiptRequest(confirmation),
                            "receiptConfirmed"));
            list.add(
                    Arguments.of(
                            "modalidade não confirmada",
                            new SelectDeliveryMethodRequest(DeliveryMethod.IN_PERSON, confirmation),
                            "confirmed"));
            list.add(
                    Arguments.of(
                            "entrega não confirmada",
                            new ConfirmInPersonDeliveryRequest(
                                    past(), "Pessoa", null, confirmation),
                            "delivered"));
            list.add(
                    Arguments.of(
                            "postagem não confirmada",
                            new ConfirmCarrierShipmentRequest(
                                    past(), "AB123456789BR", null, confirmation),
                            "posted"));
        }
        list.add(
                Arguments.of(
                        "modalidade fora do MVP",
                        new SelectDeliveryMethodRequest(DeliveryMethod.RESOURCE_LOGISTICS, true),
                        "supportedMethod"));
        list.add(
                Arguments.of(
                        "modalidade ausente",
                        new SelectDeliveryMethodRequest(null, true),
                        "method"));
        for (String person : new String[] {null, "", " ", "x".repeat(121)})
            list.add(
                    Arguments.of(
                            "recebedor inválido",
                            new ConfirmInPersonDeliveryRequest(past(), person, null, true),
                            "recipient"));
        for (LocalDate date : Arrays.asList(null, LocalDate.now().plusDays(2))) {
            list.add(
                    Arguments.of(
                            "data presencial ausente/futura",
                            new ConfirmInPersonDeliveryRequest(date, "Pessoa", null, true),
                            "date"));
            list.add(
                    Arguments.of(
                            "data postal ausente/futura",
                            new ConfirmCarrierShipmentRequest(date, "AB123456789BR", null, true),
                            "date"));
        }
        for (String code : new String[] {null, "", "AB123BR", "1234567890123", "AB12345678XBR"})
            list.add(
                    Arguments.of(
                            "rastreio inválido",
                            new ConfirmCarrierShipmentRequest(past(), code, null, true),
                            "trackingCode"));
        list.add(
                Arguments.of(
                        "observação postal longa",
                        new ConfirmCarrierShipmentRequest(
                                past(), "AB123456789BR", "x".repeat(1001), true),
                        "notes"));
        list.add(
                Arguments.of(
                        "observação presencial longa",
                        new ConfirmInPersonDeliveryRequest(
                                past(), "Pessoa", "x".repeat(1001), true),
                        "notes"));
        for (String reason : new String[] {null, "", " ", "x".repeat(1001)})
            list.add(
                    Arguments.of(
                            "recusa sem motivo válido",
                            new RejectProposalRequest(reason),
                            "reason"));
        for (String quantity : new String[] {"0", "-1", "1.5", "2147483648"})
            list.add(
                    Arguments.of(
                            "pedido fora do domínio inteiro",
                            new ManageNecessityRequest(
                                    ItemCategory.FOOD,
                                    "Alimento",
                                    new BigDecimal(quantity),
                                    NecessityPriority.HIGH,
                                    0L),
                            "quantity"));
        list.add(
                Arguments.of(
                        "pedido sem categoria",
                        new ManageNecessityRequest(null, "Alimento", 1, NecessityPriority.HIGH),
                        "category"));
        list.add(
                Arguments.of(
                        "pedido sem prioridade",
                        new ManageNecessityRequest(ItemCategory.FOOD, "Alimento", 1, null),
                        "priority"));
        for (String name : new String[] {null, "", " ", "x".repeat(151)})
            list.add(
                    Arguments.of(
                            "pedido sem nome válido",
                            new ManageNecessityRequest(
                                    ItemCategory.FOOD, name, 1, NecessityPriority.HIGH),
                            "description"));
        return list.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRequests")
    @DisplayName(
            "Dado entrada inválida pela RN, quando validada, então deve apontar o campo sem"
                    + " persistir")
    void validate_WithInvalidBusinessInput_ReportsField(
            String scenario, Object request, String field) {
        assertThat(validator.validate(request))
                .extracting(v -> v.getPropertyPath().toString())
                .contains(field);
    }

    @Test
    void validate_WithMaximumValidBoundaries_Accepts() {
        List<Object> requests =
                List.of(
                        new RegisterDonationRequest(
                                1,
                                "x".repeat(120),
                                Integer.MAX_VALUE,
                                ItemCondition.GOOD,
                                "x".repeat(1000),
                                UUID.randomUUID()),
                        new SubmitProposalRequest(1, 1, 1, "x".repeat(1000)),
                        new ConfirmInPersonDeliveryRequest(
                                past(), "x".repeat(120), "x".repeat(1000), true),
                        new ConfirmCarrierShipmentRequest(
                                past(), "ab123456789br", "x".repeat(1000), true),
                        new ManageNecessityRequest(
                                ItemCategory.FOOD,
                                "x".repeat(150),
                                Integer.MAX_VALUE,
                                NecessityPriority.LOW));
        requests.forEach(r -> assertThat(validator.validate(r)).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.1", "2147483648", "true", "\"2\"", "{}", "[]"})
    void deserialize_WithNonIntegerQuantity_RejectsWithoutTruncation(String value) {
        assertThatThrownBy(
                        () ->
                                JsonMapper.builder()
                                        .build()
                                        .readValue(
                                                "{\"quantity\":" + value + "}",
                                                DonationEditForm.class))
                .isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "1.0", "2147483647"})
    void deserialize_WithExactInteger_PreservesValue(String value) {
        assertThat(
                        JsonMapper.builder()
                                .build()
                                .readValue("{\"quantity\":" + value + "}", DonationEditForm.class)
                                .getQuantity())
                .isEqualTo(new BigDecimal(value).intValueExact());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "00000000000",
                "11111111111",
                "123",
                "123.456.789-00",
                "abcdef",
                "1234567890a"
            })
    void cpf_WithInvalidDigitsOrFormat_Rejects(String value) {
        assertThat(BrazilianDocuments.validCpf(value)).isFalse();
    }

    @Test
    void cpf_WithFictitiousValidDigitsAndMask_Normalizes() {
        assertThat(BrazilianDocuments.validCpf("123.456.789-09")).isTrue();
        assertThat(BrazilianDocuments.normalizeCpf(" 123.456.789-09 ")).isEqualTo("12345678909");
        assertThat(BrazilianDocuments.normalizeCpf(null)).isNull();
        assertThat(BrazilianDocuments.validCpf(null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "00000000000000",
                "11111111111111",
                "123",
                "12ABC34501DE00",
                "@1234567890123",
                "99.000.000/0001-01"
            })
    void cnpj_WithInvalidDigitsOrFormat_Rejects(String value) {
        assertThat(BrazilianDocuments.validCnpj(value)).isFalse();
    }

    @Test
    void cnpj_WithAlphanumericExample_NormalizesAndChecksDigits() {
        assertThat(BrazilianDocuments.normalizeCnpj(" 12.abc.345/01de-35 "))
                .isEqualTo("12ABC34501DE35");
        assertThat(BrazilianDocuments.validCnpj("12ABC34501DE35")).isTrue();
        assertThat(BrazilianDocuments.validCnpj(null)).isFalse();
        assertThat(BrazilianDocuments.normalizeCnpj(null)).isNull();
    }

    @Test
    void registration_WithNormalizationAndMatchingPassword_PreservesOnlySafeFields() {
        var f = new DonorRegistrationRequest();
        f.setFullName("  Pessoa fictícia  ");
        f.setEmail(" USER@EXAMPLE.TEST ");
        f.setPassword("Quality12!");
        f.setPasswordConfirmation("Quality12!");
        assertThat(validator.validate(f)).isEmpty();
        assertThat(f.getEmail()).isEqualTo("user@example.test");
        assertThat(f.getFullName()).isEqualTo("Pessoa fictícia");
        f.setPasswordConfirmation("Different1!");
        assertThat(validator.validate(f))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("passwordConfirmation");
        f.clearPasswords();
        assertThat(f.getPassword()).isNull();
        assertThat(f.getPasswordConfirmation()).isNull();
    }

    @Test
    void registration_WithExcessUtf8Bytes_Rejects() {
        var f = new DonorRegistrationRequest();
        f.setPassword("é".repeat(37) + "A1!");
        assertThat(validator.validate(f))
                .anySatisfy(v -> assertThat(v.getMessage()).contains("excede o limite"));
    }

    @Test
    void institution_WithFictitiousDocuments_NormalizesFields() {
        var f = new InstitutionRegistrationRequest();
        f.setCnpj("12.abc.345/01de-35");
        f.setRepresentativeCpf("123.456.789-09");
        f.setRepresentativeFullName("  Pessoa fictícia ");
        f.setEmail("fake@example.test");
        f.setPassword("Quality12!");
        f.setPasswordConfirmation("Quality12!");
        assertThat(validator.validate(f)).isEmpty();
        assertThat(f.getCnpj()).isEqualTo("12ABC34501DE35");
        assertThat(f.getRepresentativeCpf()).isEqualTo("12345678909");
    }
}

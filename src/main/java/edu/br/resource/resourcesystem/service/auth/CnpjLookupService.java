package edu.br.resource.resourcesystem.service.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.br.resource.resourcesystem.validation.BrazilianDocuments;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class CnpjLookupService {
    private final RestClient api;

    public CnpjLookupService(@Value("${resource.registration.brasil-api-url:https://brasilapi.com.br/api}") String baseUrl) {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(8));
        api = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public Company lookup(String value) {
        String cnpj = BrazilianDocuments.normalizeCnpj(value);
        if (!BrazilianDocuments.validCnpj(cnpj))
            throw new LookupException(HttpStatus.BAD_REQUEST, "Informe um CNPJ válido.");
        try {
            var response = api.get().uri("/cnpj/v1/{cnpj}", cnpj).retrieve().body(ApiCompany.class);
            if (response == null || !cnpj.equals(BrazilianDocuments.normalizeCnpj(response.cnpj()))
                    || response.legalName() == null || response.legalName().isBlank() || response.legalName().length() > 200)
                throw new LookupException(HttpStatus.SERVICE_UNAVAILABLE, "A consulta retornou dados incompletos. Tente novamente mais tarde.");
            if (!Integer.valueOf(2).equals(response.status()))
                throw new LookupException(HttpStatus.UNPROCESSABLE_CONTENT, "O CNPJ precisa estar ativo para solicitar o cadastro.");
            return new Company(cnpj, response.legalName().strip());
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404 || ex.getStatusCode().value() == 400)
                throw new LookupException(HttpStatus.UNPROCESSABLE_CONTENT, "CNPJ não encontrado. Confira o número informado.");
            throw unavailable();
        } catch (RestClientException ex) {
            throw unavailable();
        }
    }

    private LookupException unavailable() {
        return new LookupException(HttpStatus.SERVICE_UNAVAILABLE, "Consulta de CNPJ indisponível no momento. Tente novamente mais tarde.");
    }

    public record Company(String cnpj, String legalName) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ApiCompany(String cnpj, @JsonProperty("razao_social") String legalName,
                              @JsonProperty("situacao_cadastral") Integer status) {}

    public static class LookupException extends RegistrationFieldException {
        private final HttpStatus status;
        public LookupException(HttpStatus status, String message) { super("cnpj", message); this.status = status; }
        public HttpStatus status() { return status; }
    }
}

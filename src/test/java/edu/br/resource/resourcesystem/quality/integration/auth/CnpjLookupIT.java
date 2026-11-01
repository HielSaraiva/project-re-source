package edu.br.resource.resourcesystem.quality.integration.auth;

import static org.assertj.core.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;

import edu.br.resource.resourcesystem.service.auth.CnpjLookupService;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.*;

@DisplayName("RN-017 — consulta cadastral em servidor HTTP local e fictício")
class CnpjLookupIT {
    HttpServer server;
    CnpjLookupService sut;
    AtomicInteger status = new AtomicInteger(200), calls = new AtomicInteger();
    AtomicReference<String> body = new AtomicReference<>();

    @BeforeEach
    void localApi() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        body.set(
                "{\"cnpj\": \"12ABC34501DE35\", \"razao_social\": \" Organização fictícia \","
                        + " \"situacao_cadastral\": 2}");
        server.createContext(
                "/cnpj/v1/",
                exchange -> {
                    calls.incrementAndGet();
                    byte[] bytes = body.get().getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(status.get(), bytes.length);
                    exchange.getResponseBody().write(bytes);
                    exchange.close();
                });
        server.start();
        sut = new CnpjLookupService("http://127.0.0.1:" + server.getAddress().getPort());
    }

    @AfterEach
    void close() {
        server.stop(0);
    }

    @Test
    void lookup_WithActiveMatchingCompany_ReturnsNormalizedLegalName() {
        var result = sut.lookup("12.abc.345/01de-35");
        assertThat(result.cnpj()).isEqualTo("12ABC34501DE35");
        assertThat(result.legalName()).isEqualTo("Organização fictícia");
        assertThat(calls).hasValue(1);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalid", "00000000000000"})
    void lookup_WithInvalidDocument_RejectsBeforeNetwork(String value) {
        assertThatThrownBy(() -> sut.lookup(value))
                .isInstanceOfSatisfying(
                        CnpjLookupService.LookupException.class,
                        e -> assertThat(e.status().value()).isEqualTo(400));
        assertThat(calls).hasValue(0);
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 404, 429, 500, 503})
    void lookup_WithRemoteFailure_MapsSafeStatus(int remote) {
        status.set(remote);
        assertThatThrownBy(() -> sut.lookup("12ABC34501DE35"))
                .isInstanceOfSatisfying(
                        CnpjLookupService.LookupException.class,
                        e ->
                                assertThat(e.status().value())
                                        .isEqualTo(remote == 400 || remote == 404 ? 422 : 503));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{}",
                "null",
                "{\"cnpj\": \"different\"}",
                "{\"cnpj\": \"12ABC34501DE35\", \"razao_social\": \" \", \"situacao_cadastral\":"
                        + " 2}",
                "broken json"
            })
    void lookup_WithIncompleteResponse_ReturnsUnavailable(String response) {
        body.set(response);
        assertThatThrownBy(() -> sut.lookup("12ABC34501DE35"))
                .isInstanceOfSatisfying(
                        CnpjLookupService.LookupException.class,
                        e -> assertThat(e.status().value()).isEqualTo(503));
    }

    @Test
    void lookup_WithInactiveCompany_RejectsRegistration() {
        body.set(body.get().replace(": 2}", ": 1}"));
        assertThatThrownBy(() -> sut.lookup("12ABC34501DE35"))
                .isInstanceOfSatisfying(
                        CnpjLookupService.LookupException.class,
                        e -> assertThat(e.status().value()).isEqualTo(422));
    }

    @Test
    void lookup_WithDisconnectedServer_ReturnsUnavailable() {
        server.stop(0);
        assertThatThrownBy(() -> sut.lookup("12ABC34501DE35"))
                .isInstanceOfSatisfying(
                        CnpjLookupService.LookupException.class,
                        e -> assertThat(e.status().value()).isEqualTo(503));
    }
}

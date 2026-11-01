package edu.br.resource.resourcesystem.quality.integration.web;

import static org.assertj.core.api.Assertions.*;

import edu.br.resource.resourcesystem.ResourceSystemApplication;
import edu.br.resource.resourcesystem.quality.support.QualityIntegrationSupport;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(
        classes = ResourceSystemApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.config.location=classpath:quality-test.properties",
            "server.address=127.0.0.1"
        })
@AutoConfigureRestTestClient
@DisplayName("HTTP real — servidor em porta aleatória e dependências descartáveis")
class HttpServerIT extends QualityIntegrationSupport {
    @Autowired RestTestClient client;

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/",
                "/login",
                "/register/donor",
                "/register/institution",
                "/password/forgot",
                "/privacy-policy"
            })
    void page_WithRealHttpConnection_ReturnsHtml(String path) {
        client.get()
                .uri(path)
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("ReSource"));
    }

    @Test
    void api_WithoutRealSession_Returns401WithoutSecrets() {
        client.get()
                .uri("/api/notifications")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody(String.class)
                .value(
                        body ->
                                assertThat(body)
                                        .doesNotContain(
                                                "fake-quality-password", "jdbc:postgresql"));
    }

    @Test
    void missingPage_WithRealErrorDispatch_ReturnsSafe404() {
        client.get()
                .uri("/missing-quality-page")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .value(
                        body ->
                                assertThat(body)
                                        .doesNotContain(
                                                "Exception", "stackTrace", "password_hash"));
    }
}

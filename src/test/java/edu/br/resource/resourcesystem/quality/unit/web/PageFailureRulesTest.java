package edu.br.resource.resourcesystem.quality.unit.web;

import static org.assertj.core.api.Assertions.*;

import edu.br.resource.resourcesystem.config.PrivacyProperties;
import edu.br.resource.resourcesystem.controller.error.*;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.mock.web.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.*;

@DisplayName("RF-017 — classificação segura de erros de página e infraestrutura")
class PageFailureRulesTest {
    static Stream<Arguments> failures() {
        return Stream.of(
                Arguments.of(new CannotCreateTransactionException("fake"), 503),
                Arguments.of(new DataAccessResourceFailureException("fake"), 503),
                Arguments.of(
                        new Exception("wrapper", new DataAccessResourceFailureException("fake")),
                        503),
                Arguments.of(new AccessDeniedException("fake"), 403),
                Arguments.of(new ConstraintViolationException(Set.of()), 400),
                Arguments.of(new DataIntegrityViolationException("fake"), 409),
                Arguments.of(new PessimisticLockingFailureException("fake"), 409),
                Arguments.of(new OptimisticLockingFailureException("fake"), 409),
                Arguments.of(new ResponseStatusException(HttpStatus.NOT_FOUND), 404),
                Arguments.of(new IllegalStateException("fake"), 500));
    }

    @ParameterizedTest
    @MethodSource("failures")
    void handle_WithFailure_MapsStatusWithoutSecret(Exception failure, int status) {
        var req = new MockHttpServletRequest("GET", "/api/anything");
        var res = new MockHttpServletResponse();
        var result =
                (ResponseEntity<?>)
                        new PageExceptionHandler(
                                        new ErrorResponses(
                                                new PrivacyProperties("help@example.test")))
                                .handle(failure, req, res);
        assertThat(result.getStatusCode().value()).isEqualTo(status);
        assertThat(((ProblemDetail) result.getBody()).getDetail()).doesNotContain("fake");
    }

    @Test
    void handle_WithFailureInsideErrorPage_UsesPlainFallback() {
        var req = new MockHttpServletRequest("GET", "/error");
        req.setServletPath("/error");
        var result =
                (ResponseEntity<?>)
                        new PageExceptionHandler(
                                        new ErrorResponses(
                                                new PrivacyProperties("help@example.test")))
                                .handle(
                                        new IllegalStateException("secret"),
                                        req,
                                        new MockHttpServletResponse());
        assertThat(result.getStatusCode().value()).isEqualTo(500);
        assertThat(result.getBody().toString()).doesNotContain("secret");
    }
}

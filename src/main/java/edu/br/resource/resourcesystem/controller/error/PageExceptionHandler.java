package edu.br.resource.resourcesystem.controller.error;

import edu.br.resource.resourcesystem.observability.FailureDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.IdentityHashMap;

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class PageExceptionHandler {
    private final ErrorResponses errors;

    @ExceptionHandler(Exception.class)
    public Object handle(
            Exception exception, HttpServletRequest request, HttpServletResponse response) {
        int status = status(exception);
        if (status >= 500)
            log.error(
                    "event=page_failure status={} failure={}",
                    status,
                    FailureDetails.describe(exception));
        if ("/error".equals(request.getServletPath())) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(
                            "Não foi possível apresentar esta página. Acesse o início e tente"
                                    + " novamente mais tarde.");
        }
        if (exception instanceof ErrorResponse error)
            error.getHeaders()
                    .forEach(
                            (name, values) ->
                                    values.forEach(value -> response.addHeader(name, value)));
        return errors.respond(status, request, response);
    }

    private int status(Exception exception) {
        var seen = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        for (Throwable cause = exception;
                cause != null && seen.add(cause);
                cause = cause.getCause()) {
            if (cause instanceof CannotCreateTransactionException
                    || cause instanceof DataAccessResourceFailureException) return 503;
        }
        if (exception instanceof ErrorResponse error) return error.getStatusCode().value();
        if (exception instanceof AccessDeniedException) return 403;
        if (exception instanceof ConstraintViolationException) return 400;
        if (exception instanceof DataIntegrityViolationException
                || exception instanceof PessimisticLockingFailureException
                || exception instanceof OptimisticLockingFailureException) return 409;
        return 500;
    }
}

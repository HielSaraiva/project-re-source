package edu.br.resource.resourcesystem.controller.shared;

import edu.br.resource.resourcesystem.observability.FailureDetails;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice(assignableTypes = MatchOperationsController.class)
public class MatchApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ProblemDetail> business(ResponseStatusException exception) {
        log.info(
                "event=operation_rejected status={} failure={}",
                exception.getStatusCode().value(),
                exception.getClass().getSimpleName());
        return ResponseEntity.status(exception.getStatusCode())
                .body(
                        problem(
                                exception.getStatusCode(),
                                exception.getReason() == null
                                        ? "Operação indisponível."
                                        : exception.getReason()));
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        ConstraintViolationException.class,
        HttpMessageNotReadableException.class
    })
    ResponseEntity<ProblemDetail> validation(Exception exception) {
        log.info("event=validation_rejected failure={}", exception.getClass().getSimpleName());
        return ResponseEntity.badRequest()
                .body(
                        problem(
                                HttpStatus.BAD_REQUEST,
                                "Confira os campos obrigatórios, as quantidades, as datas e as confirmações do formulário."));
    }

    @ExceptionHandler({
        DataIntegrityViolationException.class,
        PessimisticLockingFailureException.class
    })
    ResponseEntity<ProblemDetail> concurrent(Exception exception) {
        log.warn("event=operation_conflict failure={}", exception.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(
                        problem(
                                HttpStatus.CONFLICT,
                                "Os dados foram alterados por outra operação. Atualize a página antes de tentar novamente."));
    }

    @ExceptionHandler({
        CannotCreateTransactionException.class,
        DataAccessResourceFailureException.class
    })
    ResponseEntity<ProblemDetail> temporarilyUnavailable(Exception exception) {
        log.error("event=database_unavailable failure={}", FailureDetails.describe(exception));
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(HttpHeaders.RETRY_AFTER, "3")
                .body(
                        problem(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "O serviço está temporariamente indisponível. Aguarde e atualize a página antes de tentar novamente."));
    }

    private ProblemDetail problem(HttpStatusCode status, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        String requestId = MDC.get("requestId");
        if (requestId != null) problem.setProperty("requestId", requestId);
        return problem;
    }
}

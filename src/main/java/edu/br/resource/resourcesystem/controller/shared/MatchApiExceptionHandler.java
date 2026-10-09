package edu.br.resource.resourcesystem.controller.shared;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(assignableTypes = MatchOperationsController.class)
public class MatchApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ProblemDetail> business(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(ProblemDetail.forStatusAndDetail(exception.getStatusCode(),exception.getReason()==null?"Operação indisponível.":exception.getReason()));
    }
    @ExceptionHandler({MethodArgumentNotValidException.class,ConstraintViolationException.class,HttpMessageNotReadableException.class})
    ResponseEntity<ProblemDetail> validation(Exception exception) {
        return ResponseEntity.badRequest().body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"Confira os campos obrigatórios, as quantidades, as datas e as confirmações do formulário."));
    }
    @ExceptionHandler({DataIntegrityViolationException.class,PessimisticLockingFailureException.class})
    ResponseEntity<ProblemDetail> concurrent(Exception exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,"Os dados foram alterados por outra operação. Atualize a página antes de tentar novamente."));
    }
}

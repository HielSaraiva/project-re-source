package edu.br.resource.resourcesystem.service.match;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class WorkflowConflictException extends ResponseStatusException {
    public WorkflowConflictException(String reason) { super(HttpStatus.CONFLICT, reason); }
}

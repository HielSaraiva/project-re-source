package edu.br.resource.resourcesystem.service.auth;

public class RegistrationFieldException extends RuntimeException {
    private final String field;

    public RegistrationFieldException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String field() {
        return field;
    }
}

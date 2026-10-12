package edu.br.resource.resourcesystem.service.auth;

public class EmailAlreadyRegisteredException extends RegistrationFieldException {
    public static final String MESSAGE =
            "Este e-mail já possui uma conta. Entre ou recupere seu acesso.";

    private final String profile;

    public EmailAlreadyRegisteredException(String profile) {
        super("email", MESSAGE);
        this.profile = profile;
    }

    public String profile() {
        return profile;
    }
}

package edu.br.resource.resourcesystem.dto.request;

import edu.br.resource.resourcesystem.validation.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MatchingPasswords
public class PasswordResetRequest implements PasswordConfirmation {
    @NotBlank
    @Pattern(regexp = "[A-Za-z0-9_-]{43}", message = "Link de recuperação inválido.")
    private String token;

    @NotBlank(message = "Informe sua nova senha.")
    @StrongPassword
    private String password;

    @NotBlank(message = "Confirme sua nova senha.")
    @Size(
            max = PasswordPolicy.MAX_LENGTH,
            message = "A confirmação deve ter no máximo 12 caracteres.")
    private String passwordConfirmation;

    public void clearPasswords() {
        password = null;
        passwordConfirmation = null;
    }
}

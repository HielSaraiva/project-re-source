package edu.br.resource.resourcesystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import edu.br.resource.resourcesystem.validation.MatchingPasswords;
import edu.br.resource.resourcesystem.validation.StrongPassword;
import edu.br.resource.resourcesystem.validation.PasswordPolicy;
import jakarta.validation.constraints.Size;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MatchingPasswords
public abstract class RegistrationCredentials implements edu.br.resource.resourcesystem.validation.PasswordConfirmation {
    @NotBlank(message = "Informe seu e-mail.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 320, message = "O e-mail deve ter no máximo 320 caracteres.")
    private String email;

    @NotBlank(message = "Informe uma senha.")
    @StrongPassword
    private String password;

    @NotBlank(message = "Confirme sua senha.")
    @Size(max = PasswordPolicy.MAX_LENGTH, message = "A confirmação deve ter no máximo " + PasswordPolicy.MAX_LENGTH + " caracteres.")
    private String passwordConfirmation;

    public void setEmail(String email) {
        this.email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }

    public void clearPasswords() {
        password = null;
        passwordConfirmation = null;
    }
}

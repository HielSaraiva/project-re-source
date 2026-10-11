package edu.br.resource.resourcesystem.dto.request;

import jakarta.validation.constraints.*;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordRecoveryRequest {
    @NotBlank(message = "Informe o e-mail cadastrado.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 320, message = "O e-mail deve ter no máximo 320 caracteres.")
    private String email;

    public void setEmail(String email) {
        this.email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }
}

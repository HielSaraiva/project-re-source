package edu.br.resource.resourcesystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DonorRegistrationRequest extends RegistrationCredentials {
    @NotBlank(message = "Informe seu nome completo.")
    @Size(max = 200, message = "O nome deve ter no máximo 200 caracteres.")
    private String fullName;

    public void setFullName(String fullName) {
        this.fullName = fullName == null ? null : fullName.strip();
    }
}

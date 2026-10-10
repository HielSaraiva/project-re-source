package edu.br.resource.resourcesystem.dto.request;

import edu.br.resource.resourcesystem.validation.BrazilianDocuments;
import jakarta.validation.constraints.NotBlank;
import edu.br.resource.resourcesystem.validation.BrazilianDocument;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class InstitutionRegistrationRequest extends RegistrationCredentials {
    @NotBlank(message = "Informe o CNPJ da instituição.")
    @BrazilianDocument(
            value = BrazilianDocument.Type.CNPJ,
            message = "Informe um CNPJ válido, com ou sem máscara.")
    private String cnpj;

    private String legalName;

    @NotBlank(message = "Informe o nome completo do representante legal.")
    @Size(max = 200, message = "O nome deve ter no máximo 200 caracteres.")
    private String representativeFullName;

    @NotBlank(message = "Informe o CPF do representante legal.")
    @BrazilianDocument(
            value = BrazilianDocument.Type.CPF,
            message = "Informe um CPF válido, com ou sem máscara.")
    private String representativeCpf;

    private MultipartFile identityDocument;
    private MultipartFile organizationDocument;

    public void setCnpj(String cnpj) {
        this.cnpj = BrazilianDocuments.normalizeCnpj(cnpj);
    }

    public void setRepresentativeCpf(String cpf) {
        this.representativeCpf = BrazilianDocuments.normalizeCpf(cpf);
    }

    public void setRepresentativeFullName(String name) {
        representativeFullName = name == null ? null : name.strip();
    }
}

package edu.br.resource.resourcesystem.controller.auth;

import lombok.RequiredArgsConstructor;
import edu.br.resource.resourcesystem.validation.PasswordPolicy;
import edu.br.resource.resourcesystem.service.auth.InstitutionDocumentStorage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = {LoginController.class, DonorRegistrationController.class, InstitutionRegistrationController.class})
@RequiredArgsConstructor
public class AuthenticationPageAdvice {
    private final ObjectProvider<ClientRegistrationRepository> clients;

    @ModelAttribute("passwordPattern")
    public String passwordPattern() { return PasswordPolicy.PATTERN; }

    @ModelAttribute("passwordPolicyMessage")
    public String passwordPolicyMessage() { return PasswordPolicy.MESSAGE; }

    @ModelAttribute("passwordMaxBytes")
    public int passwordMaxBytes() { return PasswordPolicy.MAX_BYTES; }

    @ModelAttribute("passwordMaxLength")
    public int passwordMaxLength() { return PasswordPolicy.MAX_LENGTH; }

    @ModelAttribute("passwordMinLength")
    public int passwordMinLength() { return PasswordPolicy.MIN_LENGTH; }

    @ModelAttribute("passwordByteLimitMessage")
    public String passwordByteLimitMessage() { return PasswordPolicy.BYTE_LIMIT_MESSAGE; }

    @ModelAttribute("documentMaxBytes")
    public long documentMaxBytes() { return InstitutionDocumentStorage.MAX_FILE_BYTES; }

    @ModelAttribute("googleEnabled")
    public boolean googleEnabled() {
        var registrations = clients.getIfAvailable();
        return registrations != null && registrations.findByRegistrationId("google") != null;
    }
}

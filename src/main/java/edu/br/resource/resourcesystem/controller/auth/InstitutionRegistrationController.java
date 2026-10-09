package edu.br.resource.resourcesystem.controller.auth;

import edu.br.resource.resourcesystem.dto.request.InstitutionRegistrationRequest;
import edu.br.resource.resourcesystem.security.ProfileAuthenticationSuccessHandler;
import edu.br.resource.resourcesystem.service.auth.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/register/institution")
public class InstitutionRegistrationController {
    private final InstitutionRegistrationService registrations;
    private final CnpjLookupService companies;
    private final InstitutionDocumentStorage documents;

    @InitBinder("registration")
    void registrationFields(WebDataBinder binder) {
        binder.setAllowedFields("cnpj", "email", "representativeFullName", "representativeCpf",
                "password", "passwordConfirmation", "identityDocument", "organizationDocument");
    }

    @GetMapping
    public String form(Model model, Authentication authentication) {
        if (AuthenticationPageSupport.signedIn(authentication))
            return "redirect:" + ProfileAuthenticationSuccessHandler.destination(authentication);
        model.addAttribute("registration", new InstitutionRegistrationRequest());
        return "auth/register-institution";
    }

    @GetMapping("/cnpj")
    @ResponseBody
    public ResponseEntity<?> company(@RequestParam String cnpj) {
        try {
            return ResponseEntity.ok(companies.lookup(cnpj));
        } catch (CnpjLookupService.LookupException ex) {
            return ResponseEntity.status(ex.status()).body(ProblemDetail.forStatusAndDetail(ex.status(), ex.getMessage()));
        }
    }

    @PostMapping
    public String register(@Valid @ModelAttribute("registration") InstitutionRegistrationRequest request,
            BindingResult errors, Authentication authentication) {
        if (AuthenticationPageSupport.signedIn(authentication))
            return "redirect:" + ProfileAuthenticationSuccessHandler.destination(authentication);
        var identity = validateUpload(request.getIdentityDocument(), "identityDocument", errors);
        var organization = validateUpload(request.getOrganizationDocument(), "organizationDocument", errors);
        if (!errors.hasErrors()) {
            try {
                var company = companies.lookup(request.getCnpj());
                request.setLegalName(company.legalName());
                registrations.register(request, company, identity, organization);
                request.clearPasswords();
                return "redirect:/login?profile=ong&institutionRegistered";
            } catch (RegistrationFieldException ex) {
                errors.rejectValue(ex.field(), "registration.unavailable", ex.getMessage());
            } catch (DataIntegrityViolationException ex) {
                errors.reject("registration.conflict", "Já existe cadastro com estes dados. Confira o CNPJ, o e-mail e o CPF do representante.");
            }
        }
        request.clearPasswords();
        request.setIdentityDocument(null);
        request.setOrganizationDocument(null);
        return "auth/register-institution";
    }

    private InstitutionDocumentStorage.Upload validateUpload(org.springframework.web.multipart.MultipartFile file,
            String field, BindingResult errors) {
        try { return documents.validate(file, field); }
        catch (RegistrationFieldException ex) { errors.rejectValue(field, "document.invalid", ex.getMessage()); return null; }
    }
}

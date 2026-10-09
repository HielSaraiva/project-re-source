package edu.br.resource.resourcesystem.controller.auth;

import edu.br.resource.resourcesystem.dto.request.DonorRegistrationRequest;
import edu.br.resource.resourcesystem.security.ProfileAuthenticationSuccessHandler;
import edu.br.resource.resourcesystem.service.auth.DonorRegistrationService;
import jakarta.validation.Valid;
import edu.br.resource.resourcesystem.service.auth.RegistrationFieldException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/register/donor")
public class DonorRegistrationController {
    private final DonorRegistrationService registrations;

    @InitBinder("registration")
    void registrationFields(WebDataBinder binder) {
        binder.setAllowedFields("fullName", "email", "password", "passwordConfirmation");
    }

    @GetMapping
    public String form(Model model, Authentication authentication) {
        if (AuthenticationPageSupport.signedIn(authentication)) return "redirect:" + ProfileAuthenticationSuccessHandler.destination(authentication);
        model.addAttribute("registration", new DonorRegistrationRequest());
        return "auth/register-donor";
    }

    @PostMapping
    public String register(@Valid @ModelAttribute("registration") DonorRegistrationRequest request,
            BindingResult errors, Authentication authentication) {
        if (AuthenticationPageSupport.signedIn(authentication)) return "redirect:" + ProfileAuthenticationSuccessHandler.destination(authentication);
        if (!errors.hasErrors()) {
            try {
                registrations.register(request);
                request.clearPasswords();
                return "redirect:/login?registered";
            } catch (RegistrationFieldException ex) {
                errors.rejectValue(ex.field(), "registration.unavailable", ex.getMessage());
            } catch (DataIntegrityViolationException ex) {
                errors.rejectValue("email", "email.unavailable", "Já existe uma conta com este e-mail. Utilize o login.");
            }
        }
        request.clearPasswords();
        return "auth/register-donor";
    }

}

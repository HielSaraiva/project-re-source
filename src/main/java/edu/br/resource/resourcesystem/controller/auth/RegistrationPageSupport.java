package edu.br.resource.resourcesystem.controller.auth;

import edu.br.resource.resourcesystem.service.auth.AccountEmailRegistry;
import edu.br.resource.resourcesystem.service.auth.EmailAlreadyRegisteredException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@Component
@RequiredArgsConstructor
public class RegistrationPageSupport {
    private final AccountEmailRegistry emails;

    public boolean rejectRegisteredEmail(String email, BindingResult errors, Model model) {
        if (errors.hasFieldErrors("email")) return false;
        var profile = emails.loginProfile(email);
        if (profile.isEmpty()) return false;
        existingAccount(errors, model, profile.get());
        return true;
    }

    public void existingAccount(BindingResult errors, Model model, String profile) {
        errors.rejectValue("email", "email.registered", EmailAlreadyRegisteredException.MESSAGE);
        model.addAttribute("existingAccount", true);
        model.addAttribute("existingAccountProfile", profile);
    }
}

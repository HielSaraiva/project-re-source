package edu.br.resource.resourcesystem.controller.auth;

import edu.br.resource.resourcesystem.config.PasswordRecoveryProperties;
import edu.br.resource.resourcesystem.dto.request.PasswordRecoveryRequest;
import edu.br.resource.resourcesystem.dto.request.PasswordResetRequest;
import edu.br.resource.resourcesystem.service.auth.PasswordRecoveryDelivery;
import edu.br.resource.resourcesystem.service.auth.PasswordRecoveryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/password")
@RequiredArgsConstructor
@Slf4j
public class PasswordRecoveryController {
    private final PasswordRecoveryService recovery;
    private final PasswordRecoveryDelivery delivery;
    private final PasswordRecoveryProperties settings;

    @ModelAttribute
    void privatePage(HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
        model.addAttribute("recoveryEnabled", settings.enabled());
    }

    @InitBinder("recovery")
    void recoveryFields(WebDataBinder binder) { binder.setAllowedFields("email"); }

    @InitBinder("reset")
    void resetFields(WebDataBinder binder) { binder.setAllowedFields("token", "password", "passwordConfirmation"); }

    @GetMapping("/forgot")
    public String forgot(Model model) {
        model.addAttribute("recovery", new PasswordRecoveryRequest());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot")
    public String request(@Valid @ModelAttribute("recovery") PasswordRecoveryRequest request,
            BindingResult errors, HttpServletRequest http) {
        if (errors.hasErrors()) return "auth/forgot-password";
        try { delivery.submit(request.getEmail(), http.getRemoteAddr()); }
        catch (TaskRejectedException exception) { log.warn("Fila de recuperação de senha temporariamente cheia."); }
        return "redirect:/password/forgot?sent";
    }

    @GetMapping("/reset")
    public String reset(@RequestParam(required = false) String token, Model model) {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setToken(token);
        model.addAttribute("reset", request);
        model.addAttribute("validToken", recovery.validToken(token));
        return "auth/reset-password";
    }

    @PostMapping("/reset")
    public String change(@Valid @ModelAttribute("reset") PasswordResetRequest request, BindingResult errors, Model model) {
        if (!errors.hasErrors()) {
            var profile = recovery.reset(request);
            if (profile.isPresent()) {
                request.clearPasswords();
                return "redirect:/login?passwordReset&profile=" + profile.get();
            }
        }
        request.clearPasswords();
        model.addAttribute("validToken", !errors.hasFieldErrors("token") && recovery.validToken(request.getToken()));
        return "auth/reset-password";
    }
}

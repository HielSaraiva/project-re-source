package edu.br.resource.resourcesystem.controller.auth;

import edu.br.resource.resourcesystem.security.ProfileAuthenticationSuccessHandler;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    @GetMapping("/")
    public String home(Authentication authentication) {
        return "redirect:"
                + (AuthenticationPageSupport.signedIn(authentication)
                        ? ProfileAuthenticationSuccessHandler.destination(authentication)
                        : "/login");
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(defaultValue = "donor") String profile,
            Model model,
            Authentication authentication) {
        if (AuthenticationPageSupport.signedIn(authentication)) {
            return "redirect:" + ProfileAuthenticationSuccessHandler.destination(authentication);
        }
        model.addAttribute("institutionLogin", "ong".equals(profile));
        return "auth/login";
    }
}

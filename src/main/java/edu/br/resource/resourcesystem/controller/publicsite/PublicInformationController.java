package edu.br.resource.resourcesystem.controller.publicsite;

import edu.br.resource.resourcesystem.config.PrivacyProperties;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@EnableConfigurationProperties(PrivacyProperties.class)
public class PublicInformationController {
    private final PrivacyProperties contact;

    @GetMapping("/sobre")
    public String about() {
        return "public/home";
    }

    @GetMapping("/sustentabilidade")
    public String sustainability(Model model) {
        model.addAttribute("publicSection", "sustainability");
        return "public/sustainability";
    }

    @GetMapping("/politica-de-privacidade")
    public String privacy(Model model) {
        model.addAttribute("publicSection", "privacy");
        model.addAttribute("privacyContact", contact);
        return "public/privacy";
    }
}

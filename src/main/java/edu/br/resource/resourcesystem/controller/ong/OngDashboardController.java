package edu.br.resource.resourcesystem.controller.ong;

import edu.br.resource.resourcesystem.security.CurrentActor;
import edu.br.resource.resourcesystem.service.match.MatchPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ong/dashboard")
public class OngDashboardController {
    private final MatchPageService views;
    private final CurrentActor actors;

    @GetMapping
    public String page(Authentication authentication, Model model) {
        model.addAllAttributes(views.institutionDashboard(actors.institution(authentication).getId()));
        return "ong/dashboard";
    }
}

package edu.br.resource.resourcesystem.controller.donor;

import edu.br.resource.resourcesystem.security.CurrentActor;
import edu.br.resource.resourcesystem.service.match.MatchPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/donor/dashboard")
public class DonorDashboardController {
    private final MatchPageService views;
    private final CurrentActor actors;

    @GetMapping
    public String page(Authentication authentication, Model model) {
        model.addAllAttributes(views.donorDashboard(actors.donor(authentication).getId()));
        return "donor/dashboard";
    }
}

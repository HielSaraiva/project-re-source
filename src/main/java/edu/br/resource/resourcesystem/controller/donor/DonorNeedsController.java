package edu.br.resource.resourcesystem.controller.donor;

import edu.br.resource.resourcesystem.security.CurrentActor;
import edu.br.resource.resourcesystem.service.match.MatchPageService;
import edu.br.resource.resourcesystem.dto.request.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/donor/needs")
public class DonorNeedsController {
    private final MatchPageService views;
    private final CurrentActor actors;

    @GetMapping
    public String page(Authentication authentication, Model model, @Valid @ModelAttribute NeedFilterRequest filter) {
        model.addAllAttributes(views.needs(actors.donor(authentication).getId(), filter));
        return "donor/needs";
    }
}

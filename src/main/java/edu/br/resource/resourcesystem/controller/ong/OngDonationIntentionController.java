package edu.br.resource.resourcesystem.controller.ong;

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
@RequestMapping("/ong/donations")
public class OngDonationIntentionController {
    private final MatchPageService views;
    private final CurrentActor actors;

    @GetMapping
    public String page(
            Authentication authentication,
            Model model,
            @Valid @ModelAttribute MatchFilterRequest filter) {
        model.addAllAttributes(
                views.institutionMatches(actors.institution(authentication).getId(), filter));
        return "ong/donations/list";
    }

    @GetMapping("/{protocol}")
    public String detail(
            @PathVariable String protocol, Authentication authentication, Model model) {
        model.addAllAttributes(
                views.institutionDetail(actors.institution(authentication).getId(), protocol));
        return "ong/donations/details";
    }
}

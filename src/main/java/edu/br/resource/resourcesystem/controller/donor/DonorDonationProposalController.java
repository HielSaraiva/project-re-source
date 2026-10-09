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
public class DonorDonationProposalController {
    private final MatchPageService views;
    private final CurrentActor actors;

    @GetMapping("/donor/donation/proposal")
    public String page(@RequestParam Integer need, Authentication authentication, Model model) {
        model.addAllAttributes(views.proposal(actors.donor(authentication).getId(), need));
        return "donor/donation/proposal";
    }
}

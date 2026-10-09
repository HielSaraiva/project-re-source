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
public class DonorInPersonDeliveryController {
    private final MatchPageService views;
    private final CurrentActor actors;

    @GetMapping("/donor/donation/shipping/in-person")
    public String page(@RequestParam String protocol, Authentication authentication, Model model) {
        var attributes = views.donorDetail(actors.donor(authentication).getId(), protocol);
        var match = (edu.br.resource.resourcesystem.dto.response.MatchDetailResponse) attributes.get("match");
        String actual = views.canonicalPath(match);
        if (!actual.equals("shipping/in-person")) return "redirect:/donor/donation/" + actual + "?protocol=" + match.protocol();
        model.addAllAttributes(attributes);
        return "donor/donation/in-person-delivery";
    }
}

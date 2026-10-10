package edu.br.resource.resourcesystem.controller.donor;

import edu.br.resource.resourcesystem.dto.response.MatchDetailResponse;
import edu.br.resource.resourcesystem.security.CurrentActor;
import edu.br.resource.resourcesystem.service.match.MatchPageService;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class DonorDonationController {
    private static final String PREFIX = "/donor/donation/";
    private final MatchPageService views;
    private final CurrentActor actors;

    @GetMapping({
        PREFIX + "status",
        PREFIX + "shipping",
        PREFIX + "shipping/in-person",
        PREFIX + "shipping/carrier",
        PREFIX + "completed"
    })
    public String page(
            @RequestParam String protocol,
            Authentication authentication,
            Model model,
            HttpServletRequest request) {
        var attributes = views.donorDetail(actors.donor(authentication).getId(), protocol);
        var match = (MatchDetailResponse) attributes.get("match");
        String actual = views.canonicalPath(match);
        if (!request.getServletPath().equals(PREFIX + actual)) {
            return "redirect:" + PREFIX + actual + "?protocol=" + match.protocol();
        }
        model.addAllAttributes(attributes);
        return PREFIX.substring(1)
                + switch (actual) {
                    case "shipping/in-person" -> "in-person-delivery";
                    case "shipping/carrier" -> "carrier-delivery";
                    default -> actual;
                };
    }
}

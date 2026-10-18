package edu.br.resource.resourcesystem.controller.ong;

import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import edu.br.resource.resourcesystem.security.CurrentActor;
import edu.br.resource.resourcesystem.service.necessity.NecessityManagementService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/institution/necessities")
public class OngNecessityController {
    private final NecessityManagementService necessities;
    private final CurrentActor actors;

    public OngNecessityController(
            NecessityManagementService necessities, CurrentActor actors) {
        this.necessities = necessities;
        this.actors = actors;
    }

    @GetMapping
    public String page(Authentication authentication, Model model) {
        var institution = actors.institution(authentication);
        model.addAttribute("ongName", institution.getLegalName());
        model.addAttribute("ongRole", "Administrador");
        model.addAttribute("categories", ItemCategory.values());
        model.addAttribute("necessities", necessities.list(institution.getId()));
        return "institution/necessities/list";
    }
}

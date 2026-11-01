package edu.br.resource.resourcesystem.controller.ong;

import edu.br.resource.resourcesystem.dto.request.NecessityFilterRequest;
import edu.br.resource.resourcesystem.dto.response.PageResponse;
import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import edu.br.resource.resourcesystem.model.enums.NecessityPriority;
import edu.br.resource.resourcesystem.security.CurrentActor;
import edu.br.resource.resourcesystem.service.necessity.NecessityManagementService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/institution/necessities")
public class OngNecessityController {
    private final NecessityManagementService necessities;
    private final CurrentActor actors;

    public OngNecessityController(NecessityManagementService necessities, CurrentActor actors) {
        this.necessities = necessities;
        this.actors = actors;
    }

    @GetMapping
    public String page(
            Authentication authentication,
            Model model,
            @Valid @ModelAttribute NecessityFilterRequest filter,
            @RequestParam(required = false) String notice) {
        var institution = actors.institution(authentication);
        var page = necessities.list(institution.getId(), filter);
        model.addAttribute("ongName", institution.getLegalName());
        model.addAttribute("ongRole", "Administrador");
        model.addAttribute("categories", ItemCategory.values());
        model.addAttribute("priorities", NecessityPriority.values());
        model.addAttribute("necessities", page.getContent());
        model.addAttribute(
                "pagination",
                new PageResponse<>(
                        page.getContent(),
                        page.getNumber() + 1,
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages()));
        model.addAttribute("filter", filter);
        model.addAttribute(
                "successMessage",
                switch (notice == null ? "" : notice) {
                    case "created" -> "Necessidade criada com sucesso.";
                    case "updated" -> "Alterações salvas com sucesso.";
                    case "deleted" -> "Necessidade excluída com sucesso.";
                    default -> null;
                });
        return "institution/necessities/list";
    }
}

package edu.br.resource.resourcesystem.controller.donor;

import edu.br.resource.resourcesystem.dto.request.DonationEditForm;
import edu.br.resource.resourcesystem.dto.request.DonationHistoryFilterRequest;
import edu.br.resource.resourcesystem.dto.request.NewDonationForm;
import edu.br.resource.resourcesystem.dto.response.PageResponse;
import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import edu.br.resource.resourcesystem.security.CurrentActor;
import edu.br.resource.resourcesystem.service.DonationHistoryService;
import edu.br.resource.resourcesystem.service.DonationManagementService;
import edu.br.resource.resourcesystem.service.match.WorkflowConflictException;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/donor/donations")
public class DonorDonationHistoryController {
    private static final String LIST = "redirect:/donor/donations";

    private final DonationHistoryService history;
    private final DonationManagementService management;
    private final CurrentActor actors;

    public DonorDonationHistoryController(
            DonationHistoryService history,
            DonationManagementService management,
            CurrentActor actors) {
        this.history = history;
        this.management = management;
        this.actors = actors;
    }

    @GetMapping
    public String list(
            Authentication authentication,
            @Valid @ModelAttribute DonationHistoryFilterRequest filter,
            Model model) {
        var donor = actors.donor(authentication);
        var page =
                history.findDonationsByDonor(
                        donor.getId(),
                        PageRequest.of(
                                filter.page() - 1,
                                DonationHistoryFilterRequest.PAGE_SIZE,
                                Sort.by("createdAt").descending().and(Sort.by("id").descending())));

        model.addAttribute("donorName", donor.getFullName());
        model.addAttribute("donations", page.getContent());
        model.addAttribute(
                "pagination",
                new PageResponse<>(
                        page.getContent(),
                        filter.page(),
                        DonationHistoryFilterRequest.PAGE_SIZE,
                        page.getTotalElements(),
                        page.getTotalPages()));
        model.addAttribute("pageOutOfRange", page.isEmpty() && page.getTotalElements() > 0);
        model.addAttribute("lastPage", Math.max(1, page.getTotalPages()));
        model.addAttribute("currentPage", filter.page());
        model.addAttribute("categories", ItemCategory.values());
        return "donor/donations/history";
    }

    @PostMapping
    public String register(
            @Valid @ModelAttribute("newDonation") NewDonationForm form,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirect) {
        if (result.hasErrors())
            return back(
                    redirect,
                    1,
                    "error",
                    "Não foi possível cadastrar. Confira a categoria, o título, a quantidade e o"
                            + " estado de conservação.");
        management.register(actors.donor(authentication).getId(), form);
        return back(redirect, 1, "success", "Doação cadastrada com sucesso.");
    }

    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "1") int page,
            @Valid @ModelAttribute("form") DonationEditForm form,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirect) {
        if (result.hasErrors())
            return back(
                    redirect,
                    page,
                    "error",
                    "Não foi possível salvar. Confira o título, a quantidade e o estado de"
                            + " conservação.");
        try {
            management.update(actors.donor(authentication).getId(), id, form);
            return back(redirect, page, "success", "Doação atualizada com sucesso.");
        } catch (WorkflowConflictException ex) {
            return back(redirect, page, "error", ex.getReason());
    }
}

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "1") int page,
            Authentication authentication,
            RedirectAttributes redirect) {
        try {
            management.delete(actors.donor(authentication).getId(), id);
            return back(redirect, page, "success", "Doação excluída com sucesso.");
        } catch (WorkflowConflictException ex) {
            return back(redirect, page, "error", ex.getReason());
        }
    }

    private String back(RedirectAttributes redirect, int page, String kind, String message) {
        redirect.addFlashAttribute(kind, message);
        redirect.addAttribute("page", Math.max(page, 1));
        return LIST;
    }
}

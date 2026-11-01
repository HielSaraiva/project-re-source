package edu.br.resource.resourcesystem.controller.donor;

import edu.br.resource.resourcesystem.dto.request.DonationHistoryFilterRequest;
import edu.br.resource.resourcesystem.dto.response.PageResponse;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.security.CurrentActor;
import edu.br.resource.resourcesystem.service.DonationHistoryService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/donor/donations")
public class DonorDonationHistoryController {
    private final DonationHistoryService history;
    private final CurrentActor actors;

    @GetMapping
    public String list(
            Authentication authentication,
            @Valid @ModelAttribute DonationHistoryFilterRequest filter,
            @RequestParam(required = false) String notice,
            Model model) {
        var donor = actors.donor(authentication);
        var page = history.findDonationsByDonor(donor.getId(), filter);
        model.addAttribute("donorName", donor.getFullName());
        model.addAttribute("donorId", donor.getId());
        model.addAttribute("donations", page.getContent());
        model.addAttribute(
                "pagination",
                new PageResponse<>(
                        page.getContent(),
                        page.getNumber() + 1,
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages()));
        model.addAttribute("filter", filter);
        model.addAttribute("categories", ItemCategory.values());
        model.addAttribute("conditions", ItemCondition.values());
        model.addAttribute(
                "statuses",
                DonationHistoryFilterRequest.selectableStatuses().stream()
                        .map(
                                status ->
                                        java.util.Map.of(
                                                "value",
                                                status.getValue(),
                                                "label",
                                                DonationHistoryService.label(status)))
                        .toList());
        model.addAttribute("proposalStatuses", DonationHistoryService.proposalStatuses());
        model.addAttribute(
                "successMessage",
                switch (notice == null ? "" : notice) {
                    case "created" -> "Doação cadastrada com sucesso.";
                    case "updated" -> "Alterações salvas com sucesso.";
                    case "deleted" -> "Doação excluída com sucesso.";
                    default -> null;
                });
        return "donor/donations/history";
    }
}

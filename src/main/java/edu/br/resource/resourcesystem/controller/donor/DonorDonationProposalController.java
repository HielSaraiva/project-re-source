package edu.br.resource.resourcesystem.controller.donor;

import edu.br.resource.resourcesystem.service.donor.DonorNeedsService;
import edu.br.resource.resourcesystem.service.donor.DonorInventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class DonorDonationProposalController {

    private final DonorNeedsService needsService;
    private final DonorInventoryService inventoryService;

    public DonorDonationProposalController(DonorNeedsService needsService, DonorInventoryService inventoryService) {
        this.needsService = needsService;
        this.inventoryService = inventoryService;
    }

    @GetMapping("/donor/donation/proposal")
    public String proposal(@RequestParam("need") String needId, Model model) {
        var need = needsService.findById(needId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Necessidade não encontrada"));
        model.addAttribute("donorName", "João Silva");
        model.addAttribute("need", need);
        model.addAttribute("inventoryDonations", inventoryService.registeredDonationsForCategory(need.category()));
        return "donor/donation-proposal";
    }
}

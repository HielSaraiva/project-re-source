package edu.br.resource.resourcesystem.controller.donor;

import edu.br.resource.resourcesystem.service.donor.DonorNeedsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DonorNeedsController {

    private final DonorNeedsService needsService;

    public DonorNeedsController(DonorNeedsService needsService) {
        this.needsService = needsService;
    }

    @GetMapping("/donor/needs")
    public String needs(Model model) {
        model.addAttribute("donorName", "João Silva");
        model.addAttribute("needs", needsService.needs());
        return "donor/needs";
    }
}

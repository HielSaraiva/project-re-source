package edu.br.resource.resourcesystem.controller.donor;

import edu.br.resource.resourcesystem.service.donor.DonorDonationCompletedService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DonorDonationCompletedController {
    private final DonorDonationCompletedService screenService;

    public DonorDonationCompletedController(DonorDonationCompletedService screenService) {
        this.screenService = screenService;
    }

    @GetMapping("/donor/donation/completed")
    public String completed(Model model) {
        model.addAllAttributes(screenService.screenData());
        return "donor/donation-completed";
    }
}

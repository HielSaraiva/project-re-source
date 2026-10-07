package edu.br.resource.resourcesystem.controller.donor;

import edu.br.resource.resourcesystem.service.donor.DonorDonationStatusService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DonorDonationStatusController {
    private final DonorDonationStatusService screenService;

    public DonorDonationStatusController(DonorDonationStatusService screenService) {
        this.screenService = screenService;
    }

    @GetMapping("/donor/donation/status")
    public String status(Model model) {
        model.addAllAttributes(screenService.screenData());
        return "donor/donation-status";
    }
}

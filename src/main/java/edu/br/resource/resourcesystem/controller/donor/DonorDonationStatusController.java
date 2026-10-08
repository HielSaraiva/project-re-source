package edu.br.resource.resourcesystem.controller.donor;

import edu.br.resource.resourcesystem.service.donor.DonorDonationStatusService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DonorDonationStatusController {
    private final DonorDonationStatusService screenService;

    public DonorDonationStatusController(DonorDonationStatusService screenService) {
        this.screenService = screenService;
    }

    @GetMapping("/donor/donation/status")
    public String status(@RequestParam(defaultValue = "") String status, Model model) {
        model.addAllAttributes(screenService.screenData("rejected".equals(status)));
        return "donor/donation/status";
    }
}

package edu.br.resource.resourcesystem.controller.donor;

import edu.br.resource.resourcesystem.service.donor.DonorShippingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DonorShippingController {
    private final DonorShippingService screenService;

    public DonorShippingController(DonorShippingService screenService) {
        this.screenService = screenService;
    }

    @GetMapping("/donor/donation/shipping")
    public String shipping(Model model) {
        model.addAllAttributes(screenService.screenData());
        return "donor/shipping";
    }
}

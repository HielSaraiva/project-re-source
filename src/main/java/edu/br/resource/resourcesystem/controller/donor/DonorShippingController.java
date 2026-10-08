package edu.br.resource.resourcesystem.controller.donor;

import edu.br.resource.resourcesystem.service.donor.DonorShippingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DonorShippingController {
    private final DonorShippingService screenService;

    public DonorShippingController(DonorShippingService screenService) {
        this.screenService = screenService;
    }

    @GetMapping("/donor/donation/shipping")
    public String shipping(@RequestParam(defaultValue = "") String expired, Model model) {
        model.addAllAttributes(screenService.screenData(expired));
        return "donor/donation/shipping";
    }
}

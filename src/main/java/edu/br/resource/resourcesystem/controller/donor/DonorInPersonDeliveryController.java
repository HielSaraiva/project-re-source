package edu.br.resource.resourcesystem.controller.donor;

import edu.br.resource.resourcesystem.service.donor.DonorInPersonDeliveryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DonorInPersonDeliveryController {

    private final DonorInPersonDeliveryService deliveryService;

    public DonorInPersonDeliveryController(DonorInPersonDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/donor/donation/shipping/in-person")
    public String delivery(Model model) {
        model.addAllAttributes(deliveryService.deliveryData());
        return "donor/in-person-delivery";
    }
}

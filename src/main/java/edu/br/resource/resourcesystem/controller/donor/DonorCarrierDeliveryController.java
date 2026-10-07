package edu.br.resource.resourcesystem.controller.donor;

import edu.br.resource.resourcesystem.service.donor.DonorCarrierDeliveryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DonorCarrierDeliveryController {
    private final DonorCarrierDeliveryService deliveryService;

    public DonorCarrierDeliveryController(DonorCarrierDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/donor/donation/shipping/carrier")
    public String delivery(Model model) {
        model.addAllAttributes(deliveryService.deliveryData());
        return "donor/carrier-delivery";
    }
}

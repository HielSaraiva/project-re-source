package edu.br.resource.resourcesystem.controller.ong;

import edu.br.resource.resourcesystem.service.ong.OngDonationIntentionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class OngDonationIntentionController {
    private final OngDonationIntentionService intentionsService;

    public OngDonationIntentionController(OngDonationIntentionService intentionsService) {
        this.intentionsService = intentionsService;
    }

    @GetMapping("/ong/donations")
    public String intentions(Model model) {
        model.addAllAttributes(intentionsService.screenData());
        return "ong/donations/list";
    }

    @GetMapping("/ong/donations/{id}")
    public String detail(@PathVariable String id, Model model) {
        var intention = intentionsService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Intenção de doação não encontrada"));
        model.addAttribute("ongName", "ONG EcoVida");
        model.addAttribute("ongRole", "Administrador");
        model.addAttribute("intention", intention);
        return "ong/donations/details";
    }
}

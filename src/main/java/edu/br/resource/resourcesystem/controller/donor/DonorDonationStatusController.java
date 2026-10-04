package edu.br.resource.resourcesystem.controller.donor;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DonorDonationStatusController {

    @GetMapping("/donor/donation/status")
    public String status(Model model) {
        model.addAttribute("donorName", "João Silva");
        model.addAttribute("donation", new Donation(
                "1x Monitor Dell 24\"", "Associação Vida", "28/08/2026"));
        model.addAttribute("donationDetails", List.of(
                new Detail("gift.svg", "Item Doado", "1x Monitor Dell 24\""),
                new Detail("tag.svg", "Categoria", "Eletrônicos"),
                new Detail("check-circle.svg", "Condição", "Usado - Bom estado"),
                new Detail("calendar.svg", "Data da Doação", "28/08/2026")));
        model.addAttribute("organizationDetails", List.of(
                new Detail("user.svg", "Destinatário", "Associação Vida"),
                new Detail("file-text.svg", "CNPJ", "12.345.678/0001-90"),
                new Detail("mail.svg", "Contato", "contato@associacaovida.org"),
                new Detail("clock.svg", "Prazo para Aceite", "até 05/09/2026")));
        return "donor/donation-status";
    }

    public record Donation(String item, String organization, String donationDate) {
    }

    public record Detail(String icon, String label, String value) {
    }
}

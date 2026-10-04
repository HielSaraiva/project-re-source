package edu.br.resource.resourcesystem.controller.donor;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DonorDonationCompletedController {

    @GetMapping("/donor/donation/completed")
    public String completed(Model model) {
        model.addAttribute("donorName", "João Silva");
        model.addAttribute("donation", new Donation(
                "1x Notebook Lenovo", "ONG Recomeço", "15/07/2026"));
        model.addAttribute("donationDetails", List.of(
                new Detail("gift.svg", "Item Doado", "1x Notebook Lenovo"),
                new Detail("tag.svg", "Categoria", "Eletrônicos"),
                new Detail("check-circle.svg", "Condição", "Usado - Bom estado"),
                new Detail("calendar.svg", "Data da Doação", "15/07/2026")));
        model.addAttribute("organizationDetails", List.of(
                new Detail("user.svg", "Destinatário", "ONG Recomeço"),
                new Detail("file-text.svg", "CNPJ", "98.765.432/0001-10"),
                new Detail("mail.svg", "Contato", "contato@ongrecomeco.org"),
                new Detail("clock.svg", "Data de Aceite", "18/07/2026")));
        return "donor/donation-completed";
    }

    public record Donation(String item, String organization, String donationDate) {
    }

    public record Detail(String icon, String label, String value) {
    }
}

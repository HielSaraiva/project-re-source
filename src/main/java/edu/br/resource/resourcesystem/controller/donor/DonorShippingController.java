package edu.br.resource.resourcesystem.controller.donor;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DonorShippingController {

    @GetMapping("/donor/donation/shipping")
    public String shipping(Model model) {
        model.addAttribute("donorName", "João Silva");
        model.addAttribute("donation", new Donation("1x Monitor Dell 24\"", "Instituto Esperança"));
        model.addAttribute("inPerson", new DeliveryOption(
                "Entrega Presencial",
                "Entrega Direta",
                "Leve a doação diretamente ao local da instituição de acordo com o horário estabelecido.",
                "map-pin.svg",
                "Confirmar Entrega Presencial",
                List.of(
                        new DeliveryDetail("map.svg", "Endereço de Entrega", "Rua das Flores, 123 - Centro, CEP 60000-000"),
                        new DeliveryDetail("clock.svg", "Horário de Funcionamento", "Segunda a Sexta, das 08:00 às 17:00"),
                        new DeliveryDetail("user.svg", "Responsável pelo Recebimento", "Maria Silva"))));
        model.addAttribute("mail", new DeliveryOption(
                "Envio via Correios",
                "Etiqueta de Envio",
                "Gere os dados de envio oficiais e despache o pacote em uma agência autorizada dos Correios.",
                "package.svg",
                "Gerar Dados de Postagem",
                List.of(
                        new DeliveryDetail("home.svg", "Destinatário Oficial", "Instituto Esperança"),
                        new DeliveryDetail("file-text.svg", "Documentação / CNPJ", "00.000.000/0001-00"),
                        new DeliveryDetail("truck.svg", "CEP e Logística", "60000-000 - Rua das Flores 123"))));
        return "donor/shipping";
    }

    public record Donation(String item, String organization) {
    }

    public record DeliveryOption(
            String title,
            String badge,
            String description,
            String icon,
            String action,
            List<DeliveryDetail> details) {
    }

    public record DeliveryDetail(String icon, String label, String value) {
    }
}

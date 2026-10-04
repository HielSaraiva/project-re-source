package edu.br.resource.resourcesystem.controller.donor;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DonorNeedsController {

    @GetMapping("/donor/needs")
    public String needs(Model model) {
        model.addAttribute("donorName", "João Silva");
        model.addAttribute("needs", List.of(
                new Need("Instituto Esperança", "Alta Prioridade", "Curitiba, PR", "4.2 km",
                        "Cadeira de Rodas", "Necessitamos de cadeiras de rodas motorizadas ou manuais para atendimento de idosos sob nossa tutela.",
                        "Saúde & Mobilidade", "3 unidades", "armchair.svg", "high"),
                new Need("Casa Acolher", "Média Prioridade", "São Paulo, SP", "8.1 km",
                        "Cobertores Térmicos", "Campanha do agasalho emergencial para moradores de rua acolhidos no período de inverno.",
                        "Vestuário & Cama", "50 unidades", "shirt.svg", "medium"),
                new Need("Projeto Novo Amanhã", "Alta Prioridade", "Rio de Janeiro, RJ", "12.4 km",
                        "Cestas Básicas", "Suporte imediato de alimentos não-perecíveis para famílias em situação de vulnerabilidade.",
                        "Alimentação", "120 cestas", "shopping-bag.svg", "high"),
                new Need("Rede Solidária", "Baixa Prioridade", "Belo Horizonte, MG", "6.7 km",
                        "Roupas infantis", "Precisamos de roupas em bom estado para crianças atendidas pela organização.",
                        "Vestuário", "25 peças", "heart.svg", "low"),
                new Need("Lar São José", "Média Prioridade", "Porto Alegre, RS", "15.0 km",
                        "Livros didáticos", "Livros para apoiar as atividades de reforço escolar dos acolhidos.",
                        "Educação", "40 livros", "book-open.svg", "medium"),
                new Need("Amigos da Comunidade", "Alta Prioridade", "Salvador, BA", "9.3 km",
                        "Kits de higiene", "Itens essenciais para famílias acompanhadas pelos projetos sociais.",
                        "Higiene", "80 kits", "package.svg", "high")
        ));
        return "donor/needs";
    }

    public record Need(
            String organization,
            String priority,
            String city,
            String distance,
            String item,
            String description,
            String category,
            String quantity,
            String icon,
            String priorityClass) {
    }
}

package edu.br.resource.resourcesystem.service.donor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class DonorNeedsService {

    private static final List<Need> LISTED_NEEDS = List.of(
            new Need("wheelchairs", "Instituto Esperança", "Alta Prioridade", "Curitiba, PR", "4.2 km",
                    "Cadeira de Rodas", "Necessitamos de cadeiras de rodas motorizadas ou manuais para atendimento de idosos sob nossa tutela.",
                    "Saúde & Mobilidade", "3 unidades", "armchair.svg", "high"),
            new Need("thermal-blankets", "Casa Acolher", "Média Prioridade", "São Paulo, SP", "8.1 km",
                    "Cobertores Térmicos", "Campanha do agasalho emergencial para moradores de rua acolhidos no período de inverno.",
                    "Vestuário & Cama", "50 unidades", "shirt.svg", "medium"),
            new Need("food-baskets", "Projeto Novo Amanhã", "Alta Prioridade", "Rio de Janeiro, RJ", "12.4 km",
                    "Cestas Básicas", "Suporte imediato de alimentos não-perecíveis para famílias em situação de vulnerabilidade.",
                    "Alimentação", "120 cestas", "shopping-bag.svg", "high"),
            new Need("children-clothes", "Rede Solidária", "Baixa Prioridade", "Belo Horizonte, MG", "6.7 km",
                    "Roupas infantis", "Precisamos de roupas em bom estado para crianças atendidas pela organização.",
                    "Vestuário", "25 peças", "heart.svg", "low"),
            new Need("textbooks", "Lar São José", "Média Prioridade", "Porto Alegre, RS", "15.0 km",
                    "Livros didáticos", "Livros para apoiar as atividades de reforço escolar dos acolhidos.",
                    "Educação", "40 livros", "book-open.svg", "medium"),
            new Need("hygiene-kits", "Amigos da Comunidade", "Alta Prioridade", "Salvador, BA", "9.3 km",
                    "Kits de higiene", "Itens essenciais para famílias acompanhadas pelos projetos sociais.",
                    "Higiene", "80 kits", "package.svg", "high"));

    private static final List<Need> DASHBOARD_NEEDS = List.of(
            new Need("school-chairs", "Instituto Esperança", "Alta Prioridade", "Curitiba, PR", "4.2 km",
                    "Cadeiras escolares", "Cadeiras em bom estado para equipar as salas de atividades educativas da instituição.",
                    "Móveis", "50 unidades", "armchair.svg", "high"),
            new Need("soft-blankets", "Casa do Menor", "Média Prioridade", "Curitiba, PR", "6.5 km",
                    "Cobertores macios", "Cobertores limpos e bem conservados para crianças e adolescentes acolhidos.",
                    "Vestuário & Cama", "20 unidades", "shirt.svg", "medium"),
            new Need("notebooks", "ONG Recomeço", "Alta Prioridade", "Curitiba, PR", "8.0 km",
                    "Notebooks usados", "Notebooks funcionando para apoiar cursos de inclusão digital e formação profissional.",
                    "Eletrônicos", "10 unidades", "package.svg", "high"));

    public List<Need> needs() {
        return LISTED_NEEDS;
    }

    public Optional<Need> findById(String id) {
        return Stream.concat(LISTED_NEEDS.stream(), DASHBOARD_NEEDS.stream())
                .filter(need -> need.id().equals(id))
                .findFirst();
    }

    public record Need(String id, String organization, String priority, String city, String distance,
                       String item, String description, String category, String quantity,
                       String icon, String priorityClass) {
        public int requestedQuantity() {
            return Integer.parseInt(quantity.split(" ", 2)[0]);
        }
    }
}

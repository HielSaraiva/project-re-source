package edu.br.resource.resourcesystem.service.donor;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class DonorInventoryService {

    private static final List<InventoryDonation> DONATIONS = List.of(
            new InventoryDonation("wheelchair-folding", "Cadeira de rodas dobrável", "Saúde & Mobilidade", 2,
                    "Usado (Bom estado)", "Cadeiras manuais, higienizadas e com freios funcionando.", "registered"),
            new InventoryDonation("wheelchair-new", "Cadeira de rodas nova", "Saúde & Mobilidade", 1,
                    "Novo", "Cadeira manual ainda na embalagem original.", "registered"),
            new InventoryDonation("blankets", "Cobertores de casal", "Vestuário & Cama", 8,
                    "Usado (Bom estado)", "Cobertores lavados, sem rasgos ou manchas.", "registered"),
            new InventoryDonation("thermal-blankets", "Cobertores térmicos", "Vestuário & Cama", 4,
                    "Novo", "Cobertores térmicos embalados individualmente.", "registered"),
            new InventoryDonation("food-baskets", "Cestas de alimentos", "Alimentação", 5,
                    "Novo", "Arroz, feijão, macarrão e óleo, dentro do prazo de validade.", "registered"),
            new InventoryDonation("children-clothes", "Roupas infantis", "Vestuário", 12,
                    "Usado (Bom estado)", "Peças limpas para crianças de 4 a 8 anos.", "registered"),
            new InventoryDonation("school-chairs", "Cadeiras escolares", "Móveis", 15,
                    "Usado (Bom estado)", "Cadeiras com estrutura firme e assentos conservados.", "registered"),
            new InventoryDonation("notebooks", "Notebooks Lenovo", "Eletrônicos", 3,
                    "Usado (Bom estado)", "Notebooks funcionando, com carregadores e sem dados pessoais.", "registered"),
            new InventoryDonation("monitor-pending", "Monitor Dell 24\"", "Eletrônicos", 1,
                    "Usado (Bom estado)", "Proposta já enviada à Associação Vida.", "awaiting_acceptance"),
            new InventoryDonation("chairs-empty", "Cadeiras de escritório", "Móveis", 0,
                    "Usado (Bom estado)", "Todas as unidades já foram destinadas.", "registered"));

    public List<InventoryDonation> registeredDonationsForCategory(String category) {
        return DONATIONS.stream()
                .filter(donation -> donation.category().equals(category))
                .filter(donation -> donation.status().equals("registered"))
                .filter(donation -> donation.quantity() > 0)
                .toList();
    }

    public Optional<InventoryDonation> cancellableDonationForCategory(String category) {
        return DONATIONS.stream()
                .filter(donation -> donation.id().equals("monitor-pending") && donation.category().equals(category))
                .findFirst();
    }

    public record InventoryDonation(String id, String item, String category, int quantity,
                                    String condition, String description, String status) {
    }
}

package edu.br.resource.resourcesystem.service.donor;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class DonorDashboardService {

    public Map<String, Object> dashboardData() {
        var deadline = MockDonationDeadline.recent();
        return Map.of(
                "donorName", "João Silva",
                "acceptanceDeadline", deadline.deadlineInstant(),
                "choiceDeadline", deadline.deadlineInstant(),
                "urgencies", List.of(
                        new Urgency("school-chairs", "Instituto Esperança", "Alta prioridade", "50 Cadeiras escolares", "shirt.svg", "high"),
                        new Urgency("soft-blankets", "Casa do Menor", "Média prioridade", "20 Cobertores macios", "armchair.svg", "medium"),
                        new Urgency("notebooks", "ONG Recomeço", "Alta prioridade", "10 Notebooks usados", "cpu.svg", "high")),
                "shipments", List.of(
                        new Shipment("Monitor Dell 24\"", "Associação Vida", "Aguardando aceite", "monitor.svg", "awaiting-acceptance",
                                "/donor/donation/status", "Aceite até " + deadline.deadlineLabel()),
                        new Shipment("Monitor Dell 24\"", "Instituto Esperança", "Aguardando envio", "monitor.svg", "awaiting-shipment",
                                "/donor/donation/shipping", "Escolha até " + deadline.deadlineLabel()),
                        new Shipment("Notebook Lenovo", "ONG Recomeço", "Concluído", "laptop.svg", "completed",
                                "/donor/donation/completed", "")));
    }

    public record Urgency(String needId, String institution, String priority, String item, String icon, String priorityClass) {
    }

    public record Shipment(
            String item,
            String recipient,
            String status,
            String icon,
            String statusClass,
            String detailsPath,
            String deadlineLabel) {
    }
}

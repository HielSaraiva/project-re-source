package edu.br.resource.resourcesystem.service.donor;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class DonorDashboardService {

    public Map<String, Object> dashboardData() {
        return Map.of(
                "donorName", "João Silva",
                "urgencies", List.of(
                        new Urgency("school-chairs", "Instituto Esperança", "Alta Prioridade", "50 Cadeiras escolares", "shirt.svg", "high"),
                        new Urgency("soft-blankets", "Casa do Menor", "Média Prioridade", "20 Cobertores macios", "armchair.svg", "medium"),
                        new Urgency("notebooks", "ONG Recomeço", "Alta Prioridade", "10 Notebooks usados", "cpu.svg", "high")),
                "shipments", List.of(
                        new Shipment("Monitor Dell 24\"", "Associação Vida", "Aguardando Aceite", "monitor.svg", "waiting",
                                "/donor/donation/status"),
                        new Shipment("15 Cadeiras escolares", "Instituto Esperança", "Aguardando Envio", "circle-x.svg", "shipping",
                                "/donor/donation/shipping"),
                        new Shipment("Notebook Lenovo", "ONG Recomeço", "Concluído", "laptop.svg", "completed",
                                "/donor/donation/completed")));
    }

    public record Urgency(String needId, String institution, String priority, String item, String icon, String priorityClass) {
    }

    public record Shipment(
            String item,
            String recipient,
            String status,
            String icon,
            String statusClass,
            String detailsPath) {
    }
}

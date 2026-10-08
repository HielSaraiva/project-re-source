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
                "rejectedAt", deadline.startedInstant(),
                "cancelledAt", MockDonationDeadline.expired().deadlineInstant(),
                "acceptanceDeadline", deadline.deadlineInstant(),
                "choiceDeadline", deadline.deadlineInstant(),
                "urgencies", List.of(
                        new Urgency("school-chairs", "Instituto Esperança", "Alta prioridade", "50 Cadeiras escolares", "shirt.svg", "high"),
                        new Urgency("soft-blankets", "Casa do Menor", "Média prioridade", "20 Cobertores macios", "armchair.svg", "medium"),
                        new Urgency("notebooks", "ONG Recomeço", "Alta prioridade", "10 Notebooks usados", "cpu.svg", "high")),
                "shipments", List.of(
                        new Shipment(MockDonationProtocols.PENDING_ASSOCIACAO_VIDA, "Monitor Dell 24\"", "Associação Vida", "Aguardando aceite", "monitor.svg", "awaiting-acceptance",
                                "/donor/donation/status", "Aceite até " + deadline.deadlineLabel()),
                        new Shipment(MockDonationProtocols.DELIVERY_INSTITUTO_ESPERANCA, "Monitor Dell 24\"", "Instituto Esperança", "Aguardando envio", "monitor.svg", "awaiting-shipment",
                                "/donor/donation/shipping", "Escolha até " + deadline.deadlineLabel()),
                        new Shipment(MockDonationProtocols.COMPLETED_ONG_RECOMECO, "Notebook Lenovo", "ONG Recomeço", "Concluído", "laptop.svg", "completed",
                                "/donor/donation/completed", ""),
                        new Shipment(MockDonationProtocols.REJECTED_ASSOCIACAO_VIDA, "Monitor Dell 24\"", "Associação Vida", "Recusado", "monitor.svg", "rejected",
                                "/donor/donation/status?status=rejected", ""),
                        new Shipment(MockDonationProtocols.COMPLETED_CARRIER, "Notebook Lenovo", "ONG Recomeço", "Concluído", "laptop.svg", "completed",
                                "/donor/donation/completed?method=carrier", ""),
                        new Shipment(MockDonationProtocols.EXPIRED_DELIVERIES.get("choice"), "Monitor Dell 24\"", "Instituto Esperança", "Doação cancelada", "monitor.svg", "cancelled",
                                "/donor/donation/shipping?expired=choice", ""),
                        new Shipment(MockDonationProtocols.EXPIRED_DELIVERIES.get("in_person"), "Monitor Dell 24\"", "Instituto Esperança", "Doação cancelada", "monitor.svg", "cancelled",
                                "/donor/donation/shipping?expired=in_person", ""),
                        new Shipment(MockDonationProtocols.EXPIRED_DELIVERIES.get("carrier"), "Monitor Dell 24\"", "Instituto Esperança", "Doação cancelada", "monitor.svg", "cancelled",
                                "/donor/donation/shipping?expired=carrier", "")));
    }

    public record Urgency(String needId, String institution, String priority, String item, String icon, String priorityClass) {
    }

    public record Shipment(
            String protocol,
            String item,
            String recipient,
            String status,
            String icon,
            String statusClass,
            String detailsPath,
            String deadlineLabel) {
    }
}

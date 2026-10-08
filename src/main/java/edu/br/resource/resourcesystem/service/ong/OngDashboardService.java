package edu.br.resource.resourcesystem.service.ong;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class OngDashboardService {

    private final OngDonationIntentionService intentionsService;

    public OngDashboardService(OngDonationIntentionService intentionsService) {
        this.intentionsService = intentionsService;
    }

    public Map<String, Object> dashboardData() {
        var intentions = intentionsService.intentions();
        return Map.of(
                "ongName", "ONG EcoVida",
                "ongRole", "Administrador",
                "receivedDonations", intentions.stream().filter(item -> item.status().equals("completed")).count(),
                "pendingAcceptances", intentions.stream().filter(item -> item.status().equals("awaiting_acceptance")).count(),
                "activeNeeds", 3,
                "intentions", intentions,
                "needs", List.of(
                        new RegisteredNeed("50 Cadeiras escolares", "Alta prioridade", "high", "armchair.svg"),
                        new RegisteredNeed("200 Cadernos", "Média prioridade", "medium", "book-open.svg"),
                        new RegisteredNeed("10 Notebooks", "Alta prioridade", "high", "laptop.svg")));
    }

    public record RegisteredNeed(String item, String priority, String priorityClass, String icon) {
    }
}

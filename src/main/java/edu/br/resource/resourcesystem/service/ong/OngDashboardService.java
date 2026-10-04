package edu.br.resource.resourcesystem.service.ong;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class OngDashboardService {

    public Map<String, Object> dashboardData() {
        return Map.of(
                "ongName", "ONG EcoVida",
                "ongRole", "Administrador",
                "receivedDonations", 124,
                "pendingAcceptances", 3,
                "activeNeeds", 5,
                "intentions", List.of(
                        new DonationIntention("Monitor Dell 24\"", "João Silva", "Presencial", "monitor.svg"),
                        new DonationIntention("Teclado Mecânico Keychron K2", "Mariana Souza",
                                "Logística ReSource", "keyboard.svg"),
                        new DonationIntention("Impressora Laser HP", "Carlos Eduardo", "Presencial", "printer.svg")),
                "needs", List.of(
                        new RegisteredNeed("50 Cadeiras escolares", "Alta Prioridade", "high", "armchair.svg"),
                        new RegisteredNeed("200 Cadernos", "Média", "medium", "book-open.svg"),
                        new RegisteredNeed("10 Notebooks", "Alta Prioridade", "high", "laptop.svg")));
    }

    public record DonationIntention(String item, String donor, String logistics, String icon) {
    }

    public record RegisteredNeed(String item, String priority, String priorityClass, String icon) {
    }
}

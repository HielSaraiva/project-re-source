package edu.br.resource.resourcesystem.service.ong;

import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class OngDonationIntentionService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final String MOCK_TRACKING_CODE = "AB123456789BR";

    public List<Intention> intentions() {
        var today = ZonedDateTime.now(ZoneId.of("America/Fortaleza")).withHour(10).withMinute(0).withSecond(0).withNano(0);
        var fixtures = List.of(
            new Fixture("Monitor Dell 24\"", "João Silva", "Eletrônicos", 1, "Usado (Bom estado)", "Monitores para a sala de informática", "monitor.svg", "awaiting_acceptance", 1),
            new Fixture("Teclado Keychron K2", "Mariana Souza", "Eletrônicos", 2, "Novo", "Teclados para a sala de informática", "keyboard.svg", "awaiting_acceptance", 2),
            new Fixture("Impressora HP", "Carlos Eduardo", "Eletrônicos", 1, "Usado (Bom estado)", "Impressoras para atividades educativas", "printer.svg", "awaiting_acceptance", 3),
            new Fixture("Cadeiras escolares", "Ana Costa", "Móveis", 5, "Usado (Bom estado)", "50 cadeiras escolares", "armchair.svg", "awaiting_acceptance", 4),
            new Fixture("Cadernos", "Pedro Lima", "Educação", 20, "Novo", "200 cadernos", "book-open.svg", "awaiting_acceptance", 5),
            new Fixture("Notebook Lenovo", "Beatriz Santos", "Eletrônicos", 1, "Com marcas de uso", "10 notebooks", "laptop.svg", "awaiting_acceptance", 6),
            new Fixture("Monitor LG", "Rafael Alves", "Eletrônicos", 2, "Novo", "Monitores para a sala de informática", "monitor.svg", "awaiting_shipment", 4),
            new Fixture("Cadeira de escritório", "Luiza Ferreira", "Móveis", 3, "Usado (Bom estado)", "Cadeiras para atendimento", "armchair.svg", "awaiting_delivery", 5),
            new Fixture("Notebook Acer", "Lucas Oliveira", "Eletrônicos", 1, "Usado (Bom estado)", "10 notebooks", "laptop.svg", "shipped", 6),
            new Fixture("Livros didáticos", "Camila Rocha", "Educação", 10, "Usado (Bom estado)", "Livros para reforço escolar", "book-open.svg", "awaiting_ngo_confirmation", 6),
            new Fixture("Cadernos", "Bruno Martins", "Educação", 15, "Novo", "200 cadernos", "book-open.svg", "completed", 8),
            new Fixture("Impressora Epson", "Fernanda Melo", "Eletrônicos", 1, "Com marcas de uso", "Impressoras para atividades educativas", "printer.svg", "rejected", 5),
            new Fixture("Teclados USB", "Diego Sousa", "Eletrônicos", 4, "Usado (Bom estado)", "Teclados para a sala de informática", "keyboard.svg", "cancelled", 9));
        var result = new ArrayList<Intention>();
        for (int index = 0; index < fixtures.size(); index++) {
            var fixture = fixtures.get(index);
            var created = today.minusDays(fixture.age());
            result.add(new Intention("INT-2026-" + (101 + index), fixture.item(), fixture.donor(), fixture.category(),
                fixture.quantity(), fixture.condition(), fixture.need(), fixture.icon(), fixture.status(),
                created.toOffsetDateTime().toString(), created.format(DATE), created.plusDays(7).toOffsetDateTime().toString(),
                created.plusDays(7).format(DATE), "Itens disponíveis e separados para doação.",
                "Gostaria de contribuir com as atividades da instituição.", history(fixture, created)));
        }
        return List.copyOf(result);
    }

    public Map<String, Object> screenData() {
        return Map.of("ongName", "ONG EcoVida", "ongRole", "Administrador", "intentions", intentions());
    }

    public Optional<Intention> findById(String id) {
        return intentions().stream().filter(intention -> intention.id().equals(id)).findFirst();
    }

    private List<HistoryEvent> history(Fixture fixture, ZonedDateTime created) {
        var events = new ArrayList<HistoryEvent>();
        events.add(event("Proposta enviada", fixture.donor(), "Itens e quantidade oferecidos à ONG.", created));
        var status = fixture.status();
        if (!List.of("awaiting_acceptance", "cancelled", "rejected").contains(status))
            events.add(event("Proposta aceita", "ONG EcoVida", "Doador tem 7 dias para escolher a modalidade.", created.plusDays(1)));
        if (List.of("awaiting_delivery", "awaiting_ngo_confirmation", "completed").contains(status))
            events.add(event("Entrega presencial escolhida", fixture.donor(), "Prazo de 7 dias para confirmar a entrega.", created.plusDays(2)));
        if (List.of("awaiting_ngo_confirmation", "completed").contains(status))
            events.add(event("Entrega informada", fixture.donor(), "Recebimento aguardando confirmação da ONG.", created.plusDays(3)));
        if (status.equals("shipped")) {
            events.add(event("Correios escolhido", fixture.donor(), "Prazo de 7 dias para confirmar a postagem.", created.plusDays(2)));
            events.add(event("Postagem informada", fixture.donor(), "Código informado: " + MOCK_TRACKING_CODE + ".", created.plusDays(3)));
        }
        if (status.equals("completed")) events.add(event("Recebimento confirmado", "ONG EcoVida", "Itens recebidos e doação concluída.", created.plusDays(4)));
        if (status.equals("rejected")) events.add(event("Proposta recusada", "ONG EcoVida", "O item não atende às especificações da necessidade.", created.plusDays(1)));
        if (status.equals("cancelled")) events.add(event("Proposta cancelada", "Sistema", "Prazo de aceite encerrado sem resposta da ONG.", created.plusDays(7)));
        return List.copyOf(events);
    }

    private HistoryEvent event(String title, String actor, String description, ZonedDateTime at) {
        return new HistoryEvent(title, actor, description, at.toOffsetDateTime().toString(), at.format(DATE));
    }

    private record Fixture(String item, String donor, String category, int quantity, String condition,
                           String need, String icon, String status, int age) {}
    public record HistoryEvent(String title, String actor, String description, String at, String dateLabel) {}
    public record Intention(String id, String item, String donor, String category, int quantity, String condition,
                            String need, String icon, String status, String createdAt, String createdLabel,
                            String acceptanceDeadline, String deadlineLabel, String description, String message,
                            List<HistoryEvent> history) {
        public String statusLabel() {
            return switch (status) {
                case "awaiting_acceptance" -> "Aguardando aceite";
                case "awaiting_shipment" -> "Aguardando envio";
                case "awaiting_delivery" -> "Aguardando entrega";
                case "awaiting_ngo_confirmation" -> "Aguardando confirmação da ONG";
                case "shipped" -> "Enviado";
                case "completed" -> "Concluído";
                case "rejected" -> "Recusado";
                default -> "Doação cancelada";
            };
        }
        public String statusClass() { return status.startsWith("awaiting_") ? "waiting" : status; }

        public String postedOnLabel() {
            return history.stream().filter(event -> event.title().equals("Postagem informada"))
                .findFirst().map(HistoryEvent::dateLabel).orElse("");
        }

        public String trackingCode() { return postedOnLabel().isEmpty() ? "" : MOCK_TRACKING_CODE; }

        public String deliveryMethod() {
            if (!postedOnLabel().isEmpty()) return "carrier";
            return history.stream().anyMatch(event -> event.title().equals("Entrega presencial escolhida")) ? "in_person" : "";
        }

        public String choiceDeadline() { return deadlineAfter("Proposta aceita"); }
        public String inPersonDeadline() { return deadlineAfter("Entrega presencial escolhida"); }

        private String deadlineAfter(String title) {
            return history.stream().filter(event -> event.title().equals(title)).findFirst()
                .map(event -> OffsetDateTime.parse(event.at()).plusDays(7).toString()).orElse("");
        }

        public String currentDeadlineLabel() {
            var due = switch (status) {
                case "awaiting_acceptance" -> acceptanceDeadline;
                case "awaiting_shipment" -> choiceDeadline();
                case "awaiting_delivery" -> inPersonDeadline();
                default -> "";
            };
            if (due.isEmpty()) return "";
            var prefix = switch (status) {
                case "awaiting_shipment" -> "Escolha da modalidade até ";
                case "awaiting_delivery" -> "Entrega presencial até ";
                default -> "Aceite até ";
            };
            return prefix + OffsetDateTime.parse(due).atZoneSameInstant(ZoneId.of("America/Fortaleza")).format(DATE);
        }
    }
}

package edu.br.resource.resourcesystem.service.donor;

import edu.br.resource.resourcesystem.model.view.DonationHistoryEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.br.resource.resourcesystem.model.view.DonationDetail;
import org.springframework.stereotype.Service;

@Service
public class DonorDonationCompletedService {
    public Map<String, Object> screenData() {
        return screenData(false);
    }

    public Map<String, Object> screenData(boolean carrier) {
        var model = new LinkedHashMap<String, Object>();
        model.put("donorName", "João Silva");
        var donation = new Donation(carrier ? MockDonationProtocols.COMPLETED_CARRIER : MockDonationProtocols.COMPLETED_ONG_RECOMECO, "1x Notebook Lenovo", "ONG Recomeço", "15/07/2026");
        model.put("donation", donation);
        model.put("donationDetails", List.of(
                new DonationDetail("gift.svg", "Item doado", donation.item()),
                new DonationDetail("tag.svg", "Categoria", "Eletrônicos"),
                new DonationDetail("check-circle.svg", "Condição", "Usado (Bom estado)"),
                new DonationDetail("calendar.svg", "Proposta enviada em", donation.donationDate()),
                new DonationDetail("clock.svg", "Aceite da ONG", "18/07/2026 às 10:30")));
        model.put("organizationDetails", List.of(
                new DonationDetail("user.svg", "Destinatário", donation.organization()),
                new DonationDetail("file-text.svg", "CNPJ", "98.765.432/0001-10"),
                new DonationDetail("mail.svg", "E-mail", "contato@ongrecomeco.org"),
                new DonationDetail("home.svg", "Endereço", "Rua da Solidariedade, 250 — Centro, Fortaleza/CE")));
        model.put("deliveryDetails", carrier ? List.of(
                new DonationDetail("home.svg", "Tipo de entrega", "Envio pelos Correios"),
                new DonationDetail("calendar.svg", "Data de postagem", "20/07/2026"),
                new DonationDetail("file-text.svg", "Código de rastreamento", "AB123456789BR"),
                new DonationDetail("gift.svg", "Quantidade recebida", "1 unidade — Notebook Lenovo"),
                new DonationDetail("calendar.svg", "Data de recebimento", "24/07/2026"),
                new DonationDetail("user.svg", "Recebido por", "Ana Costa — responsável pelo recebimento"),
                new DonationDetail("check-circle.svg", "Confirmação da ONG", "24/07/2026 às 14:30"),
                new DonationDetail("file-text.svg", "Observações do recebimento", "Pacote recebido com notebook e carregador em bom estado.")) : List.of(
                new DonationDetail("home.svg", "Tipo de entrega", "Entrega presencial"),
                new DonationDetail("gift.svg", "Quantidade recebida", "1 unidade — Notebook Lenovo"),
                new DonationDetail("calendar.svg", "Data de recebimento", "21/07/2026"),
                new DonationDetail("user.svg", "Recebido por", "Ana Costa — responsável pelo recebimento"),
                new DonationDetail("check-circle.svg", "Confirmação da ONG", "22/07/2026 às 09:15"),
                new DonationDetail("file-text.svg", "Observações da entrega", "Notebook entregue com carregador e recebido em bom estado.")));
        model.put("donationHistory", List.of(
                new DonationHistoryEvent("Proposta enviada", "2026-07-15T00:00:00-03:00", "João Silva", "Notebook e quantidade oferecidos à ONG."),
                new DonationHistoryEvent("Proposta aceita", "2026-07-18T10:30:00-03:00", donation.organization(), "Proposta aceita pela ONG."),
                new DonationHistoryEvent(carrier ? "Envio pelos Correios escolhido" : "Entrega presencial escolhida",
                        "2026-07-19T10:30:00-03:00", "João Silva", "Modalidade confirmada. Iniciado o prazo de 7 dias para confirmar a entrega ou a postagem."),
                new DonationHistoryEvent(carrier ? "Postagem informada" : "Entrega presencial informada",
                        carrier ? "2026-07-20T10:00:00-03:00" : "2026-07-21T10:00:00-03:00", "João Silva",
                        carrier ? "Postagem nos Correios em 20/07/2026. Rastreamento: AB123456789BR." : "Entrega em 21/07/2026. Recebido por Ana Costa."),
                new DonationHistoryEvent("Recebimento confirmado",
                        carrier ? "2026-07-24T14:30:00-03:00" : "2026-07-22T09:15:00-03:00", donation.organization(),
                        carrier ? "Recebimento em 24/07/2026, conferido por Ana Costa. Doação concluída." : "Recebimento em 21/07/2026, conferido por Ana Costa. Doação concluída.")));
        return model;
    }

    public record Donation(String protocol, String item, String organization, String donationDate) {
    }
}

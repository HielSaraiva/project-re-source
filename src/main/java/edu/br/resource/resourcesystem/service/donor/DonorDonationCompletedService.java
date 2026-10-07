package edu.br.resource.resourcesystem.service.donor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.br.resource.resourcesystem.model.view.DonationDetail;
import org.springframework.stereotype.Service;

@Service
public class DonorDonationCompletedService {
    public Map<String, Object> screenData() {
        var model = new LinkedHashMap<String, Object>();
        model.put("donorName", "João Silva");
        var donation = new Donation("1x Notebook Lenovo", "ONG Recomeço", "15/07/2026");
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
        model.put("deliveryDetails", List.of(
                new DonationDetail("home.svg", "Tipo de entrega", "Entrega presencial"),
                new DonationDetail("gift.svg", "Quantidade recebida", "1 unidade — Notebook Lenovo"),
                new DonationDetail("calendar.svg", "Data de recebimento", "21/07/2026"),
                new DonationDetail("user.svg", "Recebido por", "Ana Costa — responsável pelo recebimento"),
                new DonationDetail("check-circle.svg", "Confirmação da ONG", "22/07/2026 às 09:15"),
                new DonationDetail("file-text.svg", "Observações da entrega", "Notebook entregue com carregador e recebido em bom estado.")));
        return model;
    }

    public record Donation(String item, String organization, String donationDate) {
    }
}

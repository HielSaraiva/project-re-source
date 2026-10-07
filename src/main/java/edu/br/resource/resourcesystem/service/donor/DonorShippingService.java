package edu.br.resource.resourcesystem.service.donor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.br.resource.resourcesystem.model.view.DonationDetail;
import org.springframework.stereotype.Service;

@Service
public class DonorShippingService {
    public Map<String, Object> screenData() {
        var deadline = MockDonationDeadline.recent();
        var recipient = MockDonationRecipient.institutoEsperanca();
        var model = new LinkedHashMap<String, Object>();
        model.put("deliveryDeadlineLabel", "Até " + deadline.deadlineLabel() + " (7 dias após o aceite)");
        model.put("choiceDeadline", deadline.deadlineInstant());
        model.put("acceptedOnLabel", deadline.startedOnLabel());
        model.put("donorName", "João Silva");
        model.put("donation", new Donation("1x Monitor Dell 24\"", "Instituto Esperança"));
        model.put("inPerson", new DeliveryOption(
                "Entrega Presencial",
                "Entrega direta",
                "Leve a doação diretamente ao local da instituição de acordo com o horário estabelecido.",
                "map-pin.svg",
                "Entregar presencialmente",
                List.of(
                        new DonationDetail("map.svg", "Endereço de Entrega", recipient.address() + ", " + recipient.complement() + ", CEP " + recipient.postalCode()),
                        new DonationDetail("clock.svg", "Horário de Funcionamento", "Segunda a Sexta, das 08:00 às 17:00"),
                        new DonationDetail("user.svg", "Responsável pelo Recebimento", recipient.contactName()))));
        model.put("mail", new DeliveryOption(
                "Envio via Correios",
                "Entrega por transportadora",
                "Envie para o endereço de recebimento cadastrado pela ONG. O frete é pago por você; consulte o valor nos Correios.",
                "package.svg",
                "Enviar pelos Correios",
                List.of(
                        new DonationDetail("home.svg", "Destinatário", recipient.name()),
                        new DonationDetail("file-text.svg", "CNPJ", recipient.cnpj()),
                        new DonationDetail("truck.svg", "Endereço de recebimento", recipient.address()
                                + ", " + recipient.complement() + ", CEP " + recipient.postalCode()))));
        return model;
    }

    public record Donation(String item, String organization) {
    }

    public record DeliveryOption(
            String title,
            String badge,
            String description,
            String icon,
            String action,
            List<DonationDetail> details) {
    }
}

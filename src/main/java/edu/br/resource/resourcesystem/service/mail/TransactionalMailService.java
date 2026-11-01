package edu.br.resource.resourcesystem.service.mail;

import edu.br.resource.resourcesystem.config.PrivacyProperties;
import edu.br.resource.resourcesystem.model.entity.DonationMatch;
import edu.br.resource.resourcesystem.model.entity.Institution;
import edu.br.resource.resourcesystem.model.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class TransactionalMailService {
    private final JdbcTemplate db;
    private final PrivacyProperties privacy;

    public void welcome(User user) {
        enqueue(
                "donor-welcome:" + user.getId(),
                user.getId(),
                null,
                "Bem-vindo ao ReSource!",
                "Sua conta foi criada. Encontre necessidades de ONGs, escolha como contribuir e"
                        + " acompanhe cada etapa da sua doação.",
                "/donor/needs",
                "Encontrar necessidades",
                false);
    }

    public void registrationReceived(Institution institution) {
        enqueue(
                "institution-registration:" + institution.getId(),
                null,
                institution.getId(),
                "Recebemos o cadastro da sua ONG",
                "O cadastro e os documentos da sua instituição foram recebidos. Sua conta está"
                    + " aguardando análise documental por um administrador. Essa confirmação não"
                    + " representa a aprovação do cadastro. Para esclarecer dúvidas, entre em"
                    + " contato pelo e-mail "
                        + privacy.contactEmail()
                        + ".",
                "/login?profile=institution",
                "Acessar o ReSource",
                false);
    }

    public void passwordChanged(int accountId, boolean institution, String tokenHash) {
        enqueue(
                "password-changed:" + tokenHash,
                institution ? null : accountId,
                institution ? accountId : null,
                "Sua senha foi alterada",
                "A senha da sua conta foi redefinida com sucesso e as sessões anteriores foram"
                        + " encerradas. Se você não reconhece essa alteração, solicite uma nova"
                        + " recuperação de senha e entre em contato imediatamente pelo e-mail "
                        + privacy.contactEmail()
                        + ".",
                "/login?profile=" + (institution ? "institution" : "donor"),
                "Acessar minha conta",
                true);
    }

    public void proposalSubmitted(DonationMatch match, UUID eventId) {
        enqueue(
                "proposal-receipt:" + eventId,
                match.getDonation().getDonor().getId(),
                null,
                "Sua proposta de doação foi enviada",
                "Recebemos sua proposta para "
                        + match.getNecessity().getInstitution().getLegalName()
                        + ". Item: "
                        + match.getOfferedTitle()
                        + ". Quantidade: "
                        + match.getAllocatedQuantity()
                        + ". Protocolo: "
                        + match.getProtocol()
                        + ". Aguarde a análise da ONG. Você pode acompanhar o andamento pelo"
                        + " ReSource.",
                "/donor/donation/status?protocol=" + match.getProtocol(),
                "Acompanhar proposta",
                false);
    }

    public void queueNotifications(UUID eventId) {
        db.update(
                """
                INSERT INTO transactional_mail_outbox
                    (id, event_key, notification_id, user_id, institution_id, title, message, target_path, action_label)
                SELECT n.id, 'notification:' || n.id, n.id, n.user_id, n.institution_id,
                    n.title, n.message, n.target_path, 'Acompanhar doação'
                FROM user_notifications n WHERE n.event_id=? AND n.email_sent_at IS NULL
                ON CONFLICT (event_key) DO NOTHING
                """,
                eventId);
    }

    private void enqueue(
            String key,
            Integer userId,
            Integer institutionId,
            String title,
            String message,
            String path,
            String action,
            boolean security) {
        db.update(
                """
                INSERT INTO transactional_mail_outbox
                    (id, event_key, user_id, institution_id, title, message, target_path, action_label, security_email)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (event_key) DO NOTHING
                """,
                UUID.randomUUID(),
                key,
                userId,
                institutionId,
                title,
                message,
                path,
                action,
                security);
    }
}

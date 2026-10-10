package edu.br.resource.resourcesystem.service.notification;

import edu.br.resource.resourcesystem.config.PasswordRecoveryProperties;
import edu.br.resource.resourcesystem.service.mail.BrandedMailService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationMailDispatcher {
    private final JdbcTemplate db;
    private final BrandedMailService mail;
    private final PasswordRecoveryProperties settings;
    @Value("${resource.notifications.email-enabled:true}") private boolean enabled;
    private record Pending(UUID id,String email,String name,String title,String message,String path,int attempts) {}

    @Scheduled(fixedDelayString="${resource.notifications.mail-delay-ms:5000}",initialDelay=10000)
    public void deliver() {
        if(!enabled || !settings.enabled()) return;
        UUID claim=UUID.randomUUID();
        var batch=db.query("""
            WITH pending AS (
                SELECT id FROM user_notifications WHERE email_ready AND email_sent_at IS NULL
                    AND email_attempts<8 AND email_next_attempt_at<=now()
                    AND (email_claim_until IS NULL OR email_claim_until<=now())
                ORDER BY created_at LIMIT 5 FOR UPDATE SKIP LOCKED
            ), claimed AS (
                UPDATE user_notifications n SET email_claim=?,email_claim_until=now()+interval '5 minutes',
                    email_attempts=email_attempts+1 FROM pending WHERE n.id=pending.id RETURNING n.*
            ) SELECT n.*,coalesce(u.email,i.email) AS recipient,coalesce(u.full_name,i.legal_name) AS name
                FROM claimed n LEFT JOIN users u ON n.user_id=u.id LEFT JOIN institutions i ON n.institution_id=i.id
            """,(rs,row)->new Pending(rs.getObject("id",UUID.class),rs.getString("recipient"),rs.getString("name"),rs.getString("title"),rs.getString("message"),rs.getString("target_path"),rs.getInt("email_attempts")),claim);
        for(Pending notice:batch) {
            try {
                String url=settings.baseUrl().replaceAll("/+$","")+notice.path();
                Context context=new Context(java.util.Locale.forLanguageTag("pt-BR"));
                context.setVariable("name",notice.name());context.setVariable("title",notice.title());
                context.setVariable("message",notice.message());context.setVariable("targetUrl",url);
                mail.send(notice.email(),notice.title()+" | ReSource","Olá, "+notice.name()+".\n\n"+notice.title()+"\n"+notice.message()+"\n\nAcompanhe os detalhes: "+url+"\n\nReSource",
                        "email/notification",context);
                db.update("UPDATE user_notifications SET email_sent_at=now(),email_claim=NULL,email_claim_until=NULL WHERE id=? AND email_claim=?",notice.id(),claim);
                log.info("event=notification_email_sent notificationId={}", notice.id());
            } catch(RuntimeException failure) {
                db.update("UPDATE user_notifications SET email_claim=NULL,email_claim_until=NULL,email_next_attempt_at=now()+(? * interval '1 second') WHERE id=? AND email_claim=?",
                        Math.min(3600,30*(1<<Math.min(notice.attempts()-1,7))),notice.id(),claim);
                log.warn("event={} notificationId={} attempt={} failureType={}",
                        notice.attempts() >= 8 ? "notification_email_failed" : "notification_email_retry",
                        notice.id(), notice.attempts(), failure.getClass().getSimpleName());
            }
        }
    }
}

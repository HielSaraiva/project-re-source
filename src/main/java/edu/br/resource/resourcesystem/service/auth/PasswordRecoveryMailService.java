package edu.br.resource.resourcesystem.service.auth;

import edu.br.resource.resourcesystem.config.PasswordRecoveryProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryMailService {
    private final ObjectProvider<JavaMailSender> senders;
    private final TemplateEngine templates;
    private final PasswordRecoveryProperties settings;

    public void send(String email, String name, String token) {
        JavaMailSender sender = senders.getIfAvailable();
        if (sender == null) throw new MailSendException("SMTP não configurado.");
        String link = settings.resetUrl(token);
        long minutes = settings.tokenTtl().toMinutes();
        Context context = new Context(java.util.Locale.forLanguageTag("pt-BR"));
        context.setVariable("name", name);
        context.setVariable("resetUrl", link);
        context.setVariable("minutes", minutes);
        String html = templates.process("email/password-recovery", context);
        sender.send(message -> {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(settings.from(), "ReSource");
            helper.setTo(email);
            helper.setSubject("Redefina sua senha | ReSource");
            helper.setText("Olá, " + name + ".\n\nPara redefinir sua senha, acesse:\n" + link
                    + "\n\nO link é de uso único e expira em " + minutes + " minutos."
                    + "\nSe não solicitou a recuperação, ignore este e-mail. Sua senha permanece a mesma.\n\nReSource", html);
            // PNG derived from the site's leaf logo; CID works without a public asset URL.
            helper.addInline("resource-logo", new ClassPathResource("static/images/email/leaf.png"), "image/png");
        });
    }
}

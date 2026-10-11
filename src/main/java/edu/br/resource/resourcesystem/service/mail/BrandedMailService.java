package edu.br.resource.resourcesystem.service.mail;

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
public class BrandedMailService {
    private final ObjectProvider<JavaMailSender> senders;
    private final TemplateEngine templates;
    private final PasswordRecoveryProperties settings;

    public void send(
            String recipient, String subject, String plainText, String template, Context context) {
        JavaMailSender sender = senders.getIfAvailable();
        if (sender == null) throw new MailSendException("SMTP não configurado.");
        String html = templates.process(template, context);
        sender.send(
                message -> {
                    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                    helper.setFrom(settings.from(), "ReSource");
                    helper.setTo(recipient);
                    helper.setSubject(subject);
                    helper.setText(plainText, html);
                    helper.addInline(
                            "resource-logo",
                            new ClassPathResource("static/images/email/leaf.png"),
                            "image/png");
                });
    }
}

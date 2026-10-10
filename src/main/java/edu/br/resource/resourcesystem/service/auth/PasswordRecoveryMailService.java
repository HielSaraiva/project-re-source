package edu.br.resource.resourcesystem.service.auth;

import edu.br.resource.resourcesystem.config.PasswordRecoveryProperties;
import edu.br.resource.resourcesystem.service.mail.BrandedMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryMailService {
    private final BrandedMailService mail;
    private final PasswordRecoveryProperties settings;

    public void send(String email, String name, String token) {
        String link = settings.resetUrl(token);
        long minutes = settings.tokenTtl().toMinutes();
        Context context = new Context(java.util.Locale.forLanguageTag("pt-BR"));
        context.setVariable("name", name);
        context.setVariable("resetUrl", link);
        context.setVariable("minutes", minutes);
        mail.send(
                email,
                "Redefina sua senha | ReSource",
                "Olá, "
                        + name
                        + ".\n\nPara redefinir sua senha, acesse:\n"
                        + link
                        + "\n\nO link é de uso único e expira em "
                        + minutes
                        + " minutos."
                        + "\nSe não solicitou a recuperação, ignore este e-mail. Sua senha permanece a mesma.\n\nReSource",
                "email/password-recovery",
                context);
    }
}

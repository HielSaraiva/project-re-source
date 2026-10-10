package edu.br.resource.resourcesystem.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordRecoveryDelivery {
    private final PasswordRecoveryService recovery;

    @Async("passwordRecoveryExecutor")
    public void submit(String email, String address) {
        try { recovery.request(email, address); }
        catch (RuntimeException exception) {
            // Do not log recipients, tokens, URLs or SMTP exception messages.
            log.warn("Não foi possível enviar recuperação de senha. Tipo: {}", exception.getClass().getSimpleName());
        }
    }
}

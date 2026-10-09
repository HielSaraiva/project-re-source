package edu.br.resource.resourcesystem.observability;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthenticationLoggingListener {
    @EventListener
    public void success(AuthenticationSuccessEvent event) {
        log.debug("event=authentication_succeeded");
    }

    @EventListener
    public void failure(AbstractAuthenticationFailureEvent event) {
        log.info("event=authentication_failed errorType={}", event.getException().getClass().getSimpleName());
    }
}

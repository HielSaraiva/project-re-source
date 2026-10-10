package edu.br.resource.resourcesystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.util.Assert;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "resource.auth.google.enabled", havingValue = "true")
public class GoogleOAuthConfiguration {
    @Bean
    ClientRegistrationRepository googleClients(
            @Value("${resource.auth.google.client-id}") String clientId,
            @Value("${resource.auth.google.client-secret}") String clientSecret) {
        Assert.hasText(clientId, "Configure GOOGLE_CLIENT_ID para habilitar o login Google.");
        Assert.hasText(
                clientSecret, "Configure GOOGLE_CLIENT_SECRET para habilitar o login Google.");
        return new InMemoryClientRegistrationRepository(
                CommonOAuth2Provider.GOOGLE
                        .getBuilder("google")
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .scope("openid", "profile", "email")
                        .build());
    }
}

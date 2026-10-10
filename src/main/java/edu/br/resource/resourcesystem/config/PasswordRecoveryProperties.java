package edu.br.resource.resourcesystem.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("resource.auth.recovery")
public record PasswordRecoveryProperties(
        boolean enabled,
        @NotBlank @Email String from,
        @NotBlank String baseUrl,
        @NotNull Duration tokenTtl) {
    public PasswordRecoveryProperties {
        if (tokenTtl == null
                || tokenTtl.compareTo(Duration.ofMinutes(1)) < 0
                || tokenTtl.compareTo(Duration.ofHours(24)) > 0) {
            throw new IllegalArgumentException(
                    "A validade do link deve ser entre 1 minuto e 24 horas.");
        }
        URI uri = URI.create(baseUrl);
        if (uri.getHost() == null
                || !("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
                || uri.getRawQuery() != null
                || uri.getRawFragment() != null
                || uri.getUserInfo() != null) {
            throw new IllegalArgumentException(
                    "APP_BASE_URL deve ser uma URL HTTP(S) sem query, fragmento ou credenciais.");
        }
    }

    public String resetUrl(String token) {
        return baseUrl.replaceAll("/+$", "") + "/password/reset?token=" + token;
    }
}

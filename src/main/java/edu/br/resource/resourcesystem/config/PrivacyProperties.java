package edu.br.resource.resourcesystem.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("resource.privacy")
public record PrivacyProperties(@NotBlank @Email String contactEmail) {}

package edu.br.resource.resourcesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("ReSource API")
                .description("API do sistema ReSource — conectando doadores a organizações")
                .version("1.0.0"));
    }
}

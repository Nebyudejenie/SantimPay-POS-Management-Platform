package com.santimpay.posctl.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springdoc configuration. The generated {@code /v3/api-docs} is the contract consumed by the TS and
 * Dart client generators (API-first; ADR-011). Bearer (Keycloak JWT) security is the global default.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI posctlOpenAPI() {
        final String scheme = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("posctl API")
                        .version("v1")
                        .description("SantimPay POS Management Platform API")
                        .license(new License().name("Proprietary")))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components().addSecuritySchemes(scheme,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}

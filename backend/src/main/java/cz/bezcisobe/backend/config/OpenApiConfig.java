package cz.bezcisobe.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 configuration. Exposes a JWT bearer security scheme so the Swagger
 * UI "Authorize" dialog accepts the same tokens that the API issues from
 * {@code POST /api/auth/login}.
 *
 * <p>Swagger UI is reachable at {@code /swagger-ui.html} and the raw spec at
 * {@code /v3/api-docs}.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI bezciSobeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Běžci sobě API")
                        .version("1.0.0")
                        .description(
                                "REST API for the Běžci sobě carpooling platform. "
                                        + "Authenticated endpoints require a JWT obtained from POST /api/auth/login, "
                                        + "passed as `Authorization: Bearer <token>`.")
                        .contact(new Contact().name("Iva Fischerova").email("iva.fischerova@fidoo.com"))
                        .license(new License().name("MIT")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local dev")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT issued by /api/auth/login")));
    }
}

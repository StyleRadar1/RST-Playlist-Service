package edu.dosw.styleradar.playlist.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("RST-Playlist-Service")
                        .version("0.0.1")
                        .description("Microservicio de playlists de estilo de StyleRadar. Paquete 2 Catálogo, equipo Restrepo. " +
                                "Todas las respuestas de error siguen RFC 7807 (application/problem+json) e incluyen siempre 'code' (UPPER_SNAKE_CASE) y 'timestamp' (ISO-8601)."))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}

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
                        .description("StyleRadar fashion playlists microservice. Package 2 Catalog, Restrepo team. " +
                                "All error responses follow RFC 7807 (application/problem+json) and always include 'code' (UPPER_SNAKE_CASE) and 'timestamp' (ISO-8601)."))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}

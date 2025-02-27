package com.exercise.swiftcode.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Value("${openapi.title}")
    private String title;

    @Value("${openapi.version}")
    private String version;

    @Value("${openapi.description}")
    private String description;

    @Value("${openapi.server.url}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title(title)
                        .version(version)
                        .description(description))
                .addServersItem(new Server().url(serverUrl));
    }
}

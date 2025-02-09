package br.com.gabezy.todoapi.config;

import br.com.gabezy.todoapi.config.properties.SwaggerProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public SwaggerConfig(SwaggerProperties properties) {
        this.properties = properties;
    }

    private final SwaggerProperties properties;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(getComponents())
                .info(new Info()
                        .title(properties.getTitle())
                        .description(properties.getDescription())
                        .contact(properties.getContact())

                );
    }

    private Components getComponents() {
        return new Components()
                .addSecuritySchemes("bearer-key",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                );
    }


}

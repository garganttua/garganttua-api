package com.garganttua.api.interfaces.spring.rest.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class GGAPIInterfaceSpringRestSwaggerConfiguration {

	@Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info().title("Garganttua Example API").description(
                        "This the restfull API to access backend functions"));
    }
}

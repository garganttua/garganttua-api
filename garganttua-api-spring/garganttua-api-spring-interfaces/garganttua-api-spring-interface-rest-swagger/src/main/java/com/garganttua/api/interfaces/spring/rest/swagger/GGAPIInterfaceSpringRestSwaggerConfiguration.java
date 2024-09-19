package com.garganttua.api.interfaces.spring.rest.swagger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class GGAPIInterfaceSpringRestSwaggerConfiguration {
	
	@Value("${com.garganttua.api.interface.spring.rest.openapi.title}")
	private String openapiTitle;
	
	@Value("${com.garganttua.api.interface.spring.rest.openapi.description}")
	private String openapiDescription;

	@Value(value = "${com.garganttua.api.version}")
	private String version;
	
	@Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info().title(this.openapiTitle).description(
                        this.openapiDescription).version(this.version));
    }
}

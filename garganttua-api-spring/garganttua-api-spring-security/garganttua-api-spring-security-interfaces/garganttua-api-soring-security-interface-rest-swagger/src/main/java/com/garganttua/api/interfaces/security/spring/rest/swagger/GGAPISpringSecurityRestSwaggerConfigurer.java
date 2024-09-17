package com.garganttua.api.interfaces.security.spring.rest.swagger;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Service;

import com.garganttua.api.security.spring.core.IGGAPISpringSecurityRestConfigurer;

import io.swagger.v3.oas.models.OpenAPI;

@Service
public class GGAPISpringSecurityRestSwaggerConfigurer implements IGGAPISpringSecurityRestConfigurer {
	
	@Autowired
	private Optional<OpenAPI> openApi;
	
	@Override
	public HttpSecurity configureFilterChain(HttpSecurity http) throws Exception {
		if( this.openApi.isPresent() ) {
			http.authorizeHttpRequests().requestMatchers("/swagger-ui.html","/swagger-ui/**","/v3/**").permitAll();
		}
		return http;
	}

}

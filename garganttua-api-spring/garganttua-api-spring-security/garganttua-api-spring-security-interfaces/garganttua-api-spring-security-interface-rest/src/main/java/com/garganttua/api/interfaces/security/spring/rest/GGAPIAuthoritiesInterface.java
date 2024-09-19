package com.garganttua.api.interfaces.security.spring.rest;

import javax.inject.Inject;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.garganttua.api.security.spring.core.IGGAPISpringSecurityRestConfigurer;
import com.garganttua.api.spec.engine.IGGAPIEngine;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Authorities", description = "Service for requesting the list of authorities used by the system")
public class GGAPIAuthoritiesInterface implements IGGAPISpringSecurityRestConfigurer {

	@Inject
	private IGGAPIEngine engine;
	
	@RequestMapping(method = RequestMethod.GET, path = "/authorities")
	public ResponseEntity<?> getAuthorities(){
		return new ResponseEntity<>(this.engine.getAuthorities(),  HttpStatus.OK);
	}

	@Override
	public HttpSecurity configureFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests()
			.requestMatchers(HttpMethod.GET, "/authorities").permitAll().and();
	return http;
	}
}

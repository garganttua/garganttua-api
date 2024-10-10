package com.garganttua.api.security.spring.authentication.loginpassword.rest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.garganttua.api.interfaces.spring.rest.GGAPIResponseObject;
import com.garganttua.api.security.spring.authentication.loginpassword.GGAPILoginPasswordAuthentication;
import com.garganttua.api.security.spring.authentication.loginpassword.GGAPILoginPasswordAuthenticationRequest;
import com.garganttua.api.security.spring.authentication.loginpassword.GGAPILoginPasswordAuthenticationResponse;
import com.garganttua.api.security.spring.core.IGGAPISpringSecurityRestConfigurer;
import com.garganttua.api.security.spring.core.authorizations.IGGAPISpringSecurityAuthorizationProtocol;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.IGGAPIAuthentication;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "*")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Authentication", description = "Authentication service based on login and password. The requester must provide credentials corresponding to an authenticator entity.")
public class GGAPISpringSecurityAuthenticationLoginPasswordRestService implements IGGAPISpringSecurityRestConfigurer {

	@Autowired
	private IGGAPISecurityEngine security;
	
	@Autowired
	private Optional<IGGAPISpringSecurityAuthorizationProtocol> authorizationProtocol;
	
	@PostMapping("/authenticate")
    public void authenticate(@RequestBody GGAPILoginPasswordAuthenticationRequest authenticationRequest, HttpServletResponse response, @RequestHeader String tenantId) throws IOException {
		IGGAPIAuthentication authentication = null;
		ResponseEntity<?> responseEntity;
		try {
			authentication = this.security.authenticate(GGAPILoginPasswordAuthentication.fromRequest(tenantId, authenticationRequest));
			responseEntity = new ResponseEntity<>(GGAPILoginPasswordAuthenticationResponse.fromAuthentication(authentication).toString(), HttpStatus.CREATED);
			if( this.authorizationProtocol.isPresent() ){
				this.authorizationProtocol.get().setAuthorization(authentication.getAuthorization(), response);
			}
		} catch (GGAPIException e) {
			responseEntity = new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.BAD_REQUEST), HttpStatus.BAD_REQUEST);
		}
		this.populateResponse(responseEntity, response);

    }
	
	public void populateResponse(ResponseEntity<?> responseEntity, HttpServletResponse servletResponse)
	        throws IOException {
	    for (Map.Entry<String, List<String>> header : responseEntity.getHeaders().entrySet()) {
	        String chave = header.getKey();
	        for (String valor : header.getValue()) {
	            servletResponse.addHeader(chave, valor);                
	        }
	    }
	    servletResponse.setContentType(MediaType.APPLICATION_JSON.toString());
	    servletResponse.setStatus(responseEntity.getStatusCode().value());
	    servletResponse.getWriter().write(responseEntity.getBody().toString());
	}

	@Override
	public HttpSecurity configureFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests()
			.requestMatchers(HttpMethod.POST, "/authenticate").permitAll().and();
		return http;
	}
}

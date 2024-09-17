package com.garganttua.api.security.spring.authentication.loginpassword.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.garganttua.api.interfaces.spring.rest.GGAPIResponseObject;
import com.garganttua.api.security.spring.authentication.loginpassword.GGAPILoginPasswordAuthentication;
import com.garganttua.api.security.spring.authentication.loginpassword.GGAPILoginPasswordAuthenticationRequest;
import com.garganttua.api.security.spring.authentication.loginpassword.GGAPILoginPasswordAuthenticationResponse;
import com.garganttua.api.security.spring.core.IGGAPISpringSecurityRestConfigurer;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.IGGAPIAuthentication;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;

@RestController
@CrossOrigin(origins = "*")
public class GGAPISpringSecurityAuthenticationLoginPasswordRestService implements IGGAPISpringSecurityRestConfigurer {

	@Autowired
	private IGGAPISecurityEngine security;
	
	@PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody GGAPILoginPasswordAuthenticationRequest authenticationRequest) {
		IGGAPIAuthentication authentication = null;
		BodyBuilder response = ResponseEntity.created(null);
		try {
			authentication = this.security.authenticate(GGAPILoginPasswordAuthentication.fromRequest(authenticationRequest));
		} catch (GGAPIException e) {
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.BAD_REQUEST), HttpStatus.BAD_REQUEST);
		}

    	try {
			return response
					.header(HttpHeaders.AUTHORIZATION,"Bearer "+new String(authentication.getAuthorization().toByteArray()))
					.header("Access-Control-Expose-Headers", "Authorization")
					.contentType(MediaType.APPLICATION_JSON)
			        .body(GGAPILoginPasswordAuthenticationResponse.fromAuthentication(authentication).toString());
		} catch (GGAPIException e) {
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.UNEXPECTED_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }

	@Override
	public HttpSecurity configureFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests()
			.requestMatchers(HttpMethod.POST, "/authenticate").permitAll().and();
		return http;
	}
}

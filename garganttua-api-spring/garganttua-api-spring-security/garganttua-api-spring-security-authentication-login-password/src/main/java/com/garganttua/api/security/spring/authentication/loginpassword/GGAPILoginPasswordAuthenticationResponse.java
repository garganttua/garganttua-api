package com.garganttua.api.security.spring.authentication.loginpassword;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.spec.security.IGGAPIAuthentication;

public class GGAPILoginPasswordAuthenticationResponse {

	@JsonInclude
	private Object principal;

	public GGAPILoginPasswordAuthenticationResponse(Object principal) {
		this.principal = principal;
	}

	public static GGAPILoginPasswordAuthenticationResponse fromAuthentication(IGGAPIAuthentication authentication) {
		return new GGAPILoginPasswordAuthenticationResponse(authentication.getPrincipal());
	}
	
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this.principal);
		} catch (JsonProcessingException e) {
			return "";
		}
	}

}

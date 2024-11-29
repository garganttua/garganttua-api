package com.garganttua.api.security.authentication.interfaces.spring.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class GGAPISpringRestAuthenticationRequest {
	
	@JsonProperty
	private String principal;
	
	@JsonProperty
	private Object credentials;

}

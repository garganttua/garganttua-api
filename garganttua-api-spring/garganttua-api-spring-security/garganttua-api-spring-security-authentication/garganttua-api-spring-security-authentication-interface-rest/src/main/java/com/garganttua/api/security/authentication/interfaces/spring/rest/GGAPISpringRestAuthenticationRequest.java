package com.garganttua.api.security.authentication.interfaces.spring.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class GGAPISpringRestAuthenticationRequest {
	
	@JsonProperty
	private String principal;
	
	@JsonProperty
	private Object credentials;

}

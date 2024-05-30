package com.garganttua.api.core.security.authentication.ws.requests;

import com.garganttua.api.spec.security.IGGAPIAuthenticationRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GGAPILoginPasswordAuthenticationRequest implements IGGAPIAuthenticationRequest {
	
	private String login;
	
	private String password;

}

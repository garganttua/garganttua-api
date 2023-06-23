package com.garganttua.api.security.authentication.modes.loginpassword;

import com.garganttua.api.security.authentication.ISpringAuthenticationRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpringCrudifyLoginPasswordAuthenticationRequest implements ISpringAuthenticationRequest {
	
	private String login;
	
	private String password;

}

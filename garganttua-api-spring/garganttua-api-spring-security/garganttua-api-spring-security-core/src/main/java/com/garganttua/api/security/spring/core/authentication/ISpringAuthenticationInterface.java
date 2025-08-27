package com.garganttua.api.security.spring.core.authentication;

import org.springframework.security.authentication.AuthenticationManager;

public interface ISpringAuthenticationInterface {

	void setAuthenticationManager(AuthenticationManager manager);
	
}

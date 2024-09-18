package com.garganttua.api.security.spring.core.authentication;

import org.springframework.security.core.Authentication;

import com.garganttua.api.spec.security.IGGAPIAuthentication;

public interface IGGAPISpringAuthentication extends IGGAPIAuthentication, Authentication {
	
	void setAuthenticated(Authentication authentication);

}

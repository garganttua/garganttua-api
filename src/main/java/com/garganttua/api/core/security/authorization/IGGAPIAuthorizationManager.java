package com.garganttua.api.core.security.authorization;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.garganttua.api.core.security.GGAPISecurityException;

public interface IGGAPIAuthorizationManager {

	HttpSecurity configureFilterChain(HttpSecurity http) throws GGAPISecurityException;

	IGGAPIAuthorizationProvider getAuthorizationProvider();
	
}

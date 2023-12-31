package com.garganttua.api.security.authorization;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.garganttua.api.security.authentication.IGGAPISecurityException;

public interface IGGAPIAuthorizationManager {

	HttpSecurity configureFilterChain(HttpSecurity http) throws IGGAPISecurityException;

}

package com.garganttua.api.security;

import java.util.List;
import java.util.Optional;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;

import com.garganttua.api.security.authentication.IGGAPIAuthenticationManager;
import com.garganttua.api.security.authorization.IGGAPIAuthorization;

public interface IGGAPISecurity {

	DefaultSecurityFilterChain configureFilterChain(HttpSecurity http) throws GGAPISecurityException;

	List<IGGAPIAuthorization> getAuthorizations();
	
	Optional<IGGAPIAuthenticationManager> getAuthenticationManager();

}

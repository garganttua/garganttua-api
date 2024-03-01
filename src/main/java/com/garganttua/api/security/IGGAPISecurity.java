package com.garganttua.api.security;

import java.util.Optional;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;

import com.garganttua.api.security.authentication.IGGAPIAuthenticationManager;
import com.garganttua.api.security.authorization.IGGAPIAuthorizationManager;
import com.garganttua.api.security.owners.GGAPIOwnerVerifier;
import com.garganttua.api.security.tenants.GGAPITenantVerifier;

public interface IGGAPISecurity {

	DefaultSecurityFilterChain configureFilterChain(HttpSecurity http) throws GGAPISecurityException;
	
	Optional<IGGAPIAuthenticationManager> getAuthenticationManager();
	
	Optional<GGAPITenantVerifier> getTenantVerifier();
	
	Optional<GGAPIOwnerVerifier> getOwnerVerifier();

	Optional<IGGAPIAuthorizationManager> getAuthorizationManager();


}
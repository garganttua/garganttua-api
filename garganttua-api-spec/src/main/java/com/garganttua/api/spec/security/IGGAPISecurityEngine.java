package com.garganttua.api.spec.security;

import java.util.Optional;

public interface IGGAPISecurityEngine {

//	DefaultSecurityFilterChain configureFilterChain(HttpSecurity http) throws GGAPIException;
	
	Optional<IGGAPIAuthenticationManager> getAuthenticationManager();
	
	Optional<IGGAPITenantVerifier> getTenantVerifier();
	
	Optional<IGGAPIOwnerVerifier> getOwnerVerifier();

	Optional<IGGAPIAuthorizationManager> getAuthorizationManager();


}

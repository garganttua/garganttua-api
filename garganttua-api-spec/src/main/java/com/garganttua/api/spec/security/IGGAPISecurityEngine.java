package com.garganttua.api.spec.security;

import java.util.Optional;
import java.util.Set;

import com.garganttua.api.spec.domain.IGGAPIDomain;

public interface IGGAPISecurityEngine {

//	DefaultSecurityFilterChain configureFilterChain(HttpSecurity http) throws GGAPIException;
	
	Optional<IGGAPIAuthenticationManager> getAuthenticationManager();
	
	Optional<IGGAPITenantVerifier> getTenantVerifier();
	
	Optional<IGGAPIOwnerVerifier> getOwnerVerifier();

	Optional<IGGAPIAuthorizationManager> getAuthorizationManager();

	void init(Set<IGGAPIDomain> domains);


}

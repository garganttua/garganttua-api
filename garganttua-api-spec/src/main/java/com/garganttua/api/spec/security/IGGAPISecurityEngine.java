package com.garganttua.api.spec.security;

import java.util.Set;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIAccessRulesRegistry;

public interface IGGAPISecurityEngine {

//	DefaultSecurityFilterChain configureFilterChain(HttpSecurity http) throws GGAPIException;
	
//	Optional<IGGAPIAuthenticationManager> getAuthenticationManager();
//	
//	Optional<IGGAPITenantVerifier> getTenantVerifier();
//	
//	Optional<IGGAPIOwnerVerifier> getOwnerVerifier();
//
//	Optional<IGGAPIAuthorizationManager> getAuthorizationManager();
//
	void init(Set<IGGAPIDomain> domains);
	
	IGGAPIAccessRulesRegistry getAccessRulesRegistry();
}

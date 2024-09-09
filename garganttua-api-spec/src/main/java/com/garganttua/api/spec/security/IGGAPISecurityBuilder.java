package com.garganttua.api.spec.security;

import java.util.Set;

import com.garganttua.api.spec.domain.IGGAPIDomain;

public interface IGGAPISecurityBuilder {
	
	IGGAPISecurityBuilder authenticationManager(IGGAPIAuthenticationManager manager); 
	IGGAPISecurityBuilder authorizationManager(IGGAPIAuthorizationManager manager);
	IGGAPISecurityBuilder tenantVerifier(IGGAPITenantVerifier verifier);
	IGGAPISecurityBuilder ownerVerifier(IGGAPIOwnerVerifier verifier);
	IGGAPISecurityBuilder domains(Set<IGGAPIDomain> domains); 
	
	IGGAPISecurityEngine build();
}

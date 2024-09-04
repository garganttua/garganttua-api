package com.garganttua.api.spec.security;

import java.util.Optional;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIAuthenticationManager {

//	IGGAPISecurity configureSecurity(IGGAPISecurity security) throws GGAPIException;

	Optional<IGGAPIPasswordEncoder> getPasswordEncoder();

	Object applySecurityOnAuthenticatorEntity(Object entity) throws GGAPIException;
	
	IGGAPIAuthenticator getAuthenticatorFromOwnerId(String tenantId, String ownerId) throws GGAPIException;
	
}

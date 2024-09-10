package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIAuthenticationManager {

	Object applySecurityOnAuthenticatorEntity(Object entity) throws GGAPIException;

	IGGAPIAuthentication authenticate(IGGAPIAuthentication entity) throws GGAPIException;
	
}

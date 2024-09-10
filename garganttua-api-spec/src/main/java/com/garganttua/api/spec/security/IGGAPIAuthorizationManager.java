package com.garganttua.api.spec.security;

public interface IGGAPIAuthorizationManager {

	IGGAPIAuthorization validateAuthorization(byte[] authorization);

	IGGAPIAuthentication createAuthorization(IGGAPIAuthentication entity);
	
}

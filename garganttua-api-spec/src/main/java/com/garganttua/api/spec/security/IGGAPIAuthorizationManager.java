package com.garganttua.api.spec.security;

public interface IGGAPIAuthorizationManager {

	IGGAPIAuthorization validateAuthorization(byte[] authorization);

	IGGAPIAuthorization createAuthorization(Object entity);
	
}

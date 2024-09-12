package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIAuthorizationManager {

	IGGAPIAuthorization validateAuthorization(byte[] authorization);

	IGGAPIAuthentication createAuthorization(IGGAPIAuthentication entity) throws GGAPIException;
	
}

package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIAuthorizationManager {

	IGGAPIAuthentication validateAuthorization(byte[] authorization) throws GGAPIException;

	IGGAPIAuthentication createAuthorization(IGGAPIAuthentication entity) throws GGAPIException;
	
}

package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIAuthorizationProvider {

	IGGAPIToken getAuthorization(IGGAPIAuthentication authentication) throws GGAPIException;

	IGGAPIToken validateAuthorization(byte[] token) throws GGAPIException;

}

package com.garganttua.api.core.security.authorization;

import org.springframework.security.core.Authentication;

import com.garganttua.api.core.security.authorization.tokens.GGAPIToken;

public interface IGGAPIAuthorizationProvider {

	GGAPIToken getAuthorization(Authentication authentication) throws GGAPIAuthorizationProviderException;

	GGAPIToken validateAuthorization(byte[] token) throws GGAPIAuthorizationProviderException;

}

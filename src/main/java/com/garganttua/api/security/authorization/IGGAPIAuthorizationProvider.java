package com.garganttua.api.security.authorization;

import org.springframework.security.core.Authentication;

import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.security.authorization.tokens.GGAPIToken;
import com.garganttua.api.security.authorization.tokens.jwt.GGAPITokenExpired;
import com.garganttua.api.security.authorization.tokens.jwt.GGAPITokenNotFoundException;
import com.garganttua.api.security.keys.GGAPIKeyExpiredException;

public interface IGGAPIAuthorizationProvider {

	GGAPIToken getAuthorization(Authentication authentication) throws GGAPIKeyExpiredException, GGAPIEngineException, GGAPIEntityException;

	GGAPIToken validateAuthorization(byte[] token) throws GGAPIKeyExpiredException, GGAPITokenNotFoundException, GGAPIEngineException, GGAPITokenExpired;

}

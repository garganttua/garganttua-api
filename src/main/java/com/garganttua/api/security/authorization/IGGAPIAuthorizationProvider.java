package com.garganttua.api.security.authorization;

import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.security.authentication.dao.AbstractGGAPIUserDetails;
import com.garganttua.api.security.authorization.token.GGAPIToken;
import com.garganttua.api.security.authorization.token.jwt.GGAPITokenExpired;
import com.garganttua.api.security.authorization.token.jwt.GGAPITokenNotFoundException;
import com.garganttua.api.security.keys.GGAPIKeyExpiredException;

public interface IGGAPIAuthorizationProvider {

	GGAPIToken getAuthorization(AbstractGGAPIUserDetails userDetails) throws GGAPIKeyExpiredException, GGAPIEngineException;

	String getUserNameFromAuthorization(String token) throws GGAPIKeyExpiredException, GGAPIEngineException;

	boolean validateAuthorization(String token, AbstractGGAPIUserDetails userDetails) throws GGAPIKeyExpiredException, GGAPITokenNotFoundException, GGAPIEngineException, GGAPITokenExpired;

}

package com.garganttua.api.core.security.authorization.tokens.jwt;

import com.garganttua.api.core.security.authorization.tokens.GGAPIToken;

public class GGAPIJwtTokenProvider extends AbstractGGAPIJwtTokenProvider {

	@Override
	protected void storeToken(GGAPIToken token) {
	
	}

	@Override
	protected GGAPIToken findToken(GGAPIToken token) {
		return token;
	}


}

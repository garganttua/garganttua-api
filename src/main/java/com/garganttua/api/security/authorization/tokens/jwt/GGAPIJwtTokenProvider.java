package com.garganttua.api.security.authorization.tokens.jwt;

import com.garganttua.api.security.authorization.tokens.GGAPIToken;

public class GGAPIJwtTokenProvider extends AbstractGGAPIJwtTokenProvider {

	@Override
	protected void storeToken(GGAPIToken token) {
	
	}

	@Override
	protected GGAPIToken findToken(GGAPIToken token) throws GGAPITokenNotFoundException {
		return token;
	}


}

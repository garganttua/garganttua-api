package com.garganttua.api.security.authorization.tokens.jwt;

import java.util.Optional;

import com.garganttua.api.security.authorization.tokens.GGAPIToken;

public class GGAPIJwtDBTokenProvider extends AbstractGGAPIJwtTokenProvider {

	private Optional<IGGAPIDBTokenKeeper> tokenKeeper;

	@Override
	protected void storeToken(GGAPIToken token) {
		this.tokenKeeper.get().store(token);
	}

	@Override
	protected GGAPIToken findToken(GGAPIToken token) throws GGAPITokenNotFoundException {
		return this.tokenKeeper.get().findOne(token);
	}

	public void setTokenKeeper(Optional<IGGAPIDBTokenKeeper> tokenKeeper) {
		this.tokenKeeper = tokenKeeper;
	}
}

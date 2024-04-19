package com.garganttua.api.core.security.authorization.tokens.jwt;

import java.util.Optional;

import com.garganttua.api.core.repository.GGAPIRepositoryException;
import com.garganttua.api.core.security.authorization.GGAPIAuthorizationProviderException;
import com.garganttua.api.core.security.authorization.tokens.GGAPIToken;

public class GGAPIJwtDBTokenProvider extends AbstractGGAPIJwtTokenProvider {

	private Optional<IGGAPIDBTokenKeeper> tokenKeeper;

	@Override
	protected void storeToken(GGAPIToken token) throws GGAPIAuthorizationProviderException {
		try {
			this.tokenKeeper.get().store(token);
		} catch (GGAPIRepositoryException e) {
			throw new GGAPIAuthorizationProviderException(e);
		}
	}

	@Override
	protected GGAPIToken findToken(GGAPIToken token) throws GGAPIAuthorizationProviderException {
		try {
			return this.tokenKeeper.get().findOne(token);
		} catch (GGAPIRepositoryException e) {
			throw new GGAPIAuthorizationProviderException(e);
		}
	}

	public void setTokenKeeper(Optional<IGGAPIDBTokenKeeper> tokenKeeper) {
		this.tokenKeeper = tokenKeeper;
	}
}

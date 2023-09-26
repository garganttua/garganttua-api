package com.garganttua.api.security.authorization.token.jwt;

import org.springframework.beans.factory.annotation.Autowired;

import com.garganttua.api.security.authorization.token.GGAPIToken;

public class GGAPIJwtDBTokenProvider extends AbstractGGAPIJwtTokenProvider {

	@Autowired 
	private IGGAPIDBTokenKeeper tokenKeeper;


	@Override
	protected void storeToken(GGAPIToken token) {
		this.tokenKeeper.store(token);
	}


	@Override
	protected GGAPIToken findToken(GGAPIToken token) throws GGAPITokenNotFoundException {
		return this.tokenKeeper.findOne(token);
	}

}

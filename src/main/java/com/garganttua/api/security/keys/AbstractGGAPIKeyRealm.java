package com.garganttua.api.security.keys;

import java.util.Date;

import io.jsonwebtoken.SignatureAlgorithm;

public abstract class AbstractGGAPIKeyRealm implements IGGAPIKeyRealm {
	
	public AbstractGGAPIKeyRealm() {
		
	}
	
	public AbstractGGAPIKeyRealm(String algo, GGAPIKeyExpiration expiration) {
		this.getKey(algo);	
	}

	protected abstract void getKey(String algo);

	@Override
	public GGAPIKey getCipheringKey() throws GGAPIKeyExpiredException {
		GGAPIKey key = this.getCipheringKey_();
		if( key.getExpiration() != null ) {
			if( new Date().after(key.getExpiration()) ) {
				throw new GGAPIKeyExpiredException("The key has expired");
			}
		}
		return key;
	}

	protected abstract GGAPIKey getCipheringKey_();

	@Override
	public GGAPIKey getUncipheringKey() throws GGAPIKeyExpiredException {
		GGAPIKey key = this.getUncipheringKey_();
		if( key.getExpiration() != null ) {
			if( new Date().after(key.getExpiration()) ) {
				throw new GGAPIKeyExpiredException("The key has expired");
			}
		}
		
		return key;
	}

	protected abstract GGAPIKey getUncipheringKey_();
}

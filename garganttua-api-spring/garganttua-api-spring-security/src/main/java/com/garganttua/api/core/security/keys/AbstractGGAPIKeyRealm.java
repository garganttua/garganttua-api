package com.garganttua.api.core.security.keys;

import java.util.Date;

import com.garganttua.api.security.spring.keys.domain.GGAPIKey;
import com.garganttua.api.security.spring.keys.domain.GGAPIKeyExpiration;
import com.garganttua.api.security.spring.keys.domain.GGAPIKeyExpiredException;
import com.garganttua.api.security.spring.keys.domain.IGGAPIKeyRealm;

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
	
	@Override
	public boolean equals(IGGAPIKeyRealm object) {
		try {
			return this.getAlgo().equals(((IGGAPIKeyRealm) object).getAlgo()) &&
					this.getName().equals(((IGGAPIKeyRealm) object).getName()) && 
					this.getCipheringKey().equals(((IGGAPIKeyRealm) object).getCipheringKey()) &&
					this.getUncipheringKey().equals(((IGGAPIKeyRealm) object).getUncipheringKey());
		} catch (GGAPIKeyExpiredException e) {
			e.printStackTrace();
			return false;
		}
	}
}

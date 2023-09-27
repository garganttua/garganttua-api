package com.garganttua.api.security.keys;

import org.springframework.beans.factory.annotation.Autowired;

public class GGAPIDBKeyManager implements IGGAPIKeyManager {
	
	@Autowired
	private IGGAPIDBKeyKeeper keyKeeper;

	@Override
	public GGAPIKey getKeyForCiphering(String realm) throws GGAPIKeyExpiredException {
		IGGAPIKeyRealm realm_ = this.keyKeeper.getRelam(realm);
		if( realm_ != null ) {
			return realm_.getCipheringKey();
		}
		
		return null;
	}

	@Override
	public GGAPIKey getKeyForUnciphering(String realm) throws GGAPIKeyExpiredException {
		IGGAPIKeyRealm realm_ = this.keyKeeper.getRelam(realm);
		if( realm_ != null ) {
			return realm_.getUncipheringKey();
		}
		return null;
	}

	@Override
	public void renew(IGGAPIKeyRealm realm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createRealm(IGGAPIKeyRealm realm) throws GGAPIKeyExpiredException {
		this.keyKeeper.createRealm(realm);
	}

}

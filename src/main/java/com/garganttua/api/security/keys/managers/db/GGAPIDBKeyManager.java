package com.garganttua.api.security.keys.managers.db;

import java.util.Optional;

import com.garganttua.api.security.keys.GGAPIKey;
import com.garganttua.api.security.keys.GGAPIKeyExpiredException;
import com.garganttua.api.security.keys.IGGAPIKeyManager;
import com.garganttua.api.security.keys.IGGAPIKeyRealm;

import lombok.Setter;

public class GGAPIDBKeyManager implements IGGAPIKeyManager {
	
	@Setter
	private Optional<IGGAPIDBKeyKeeper> keyKeeper;

	@Override
	public GGAPIKey getKeyForCiphering(String realm) throws GGAPIKeyExpiredException {
		IGGAPIKeyRealm realm_ = this.keyKeeper.get().getRealm(realm);
		if( realm_ != null ) {
			return realm_.getCipheringKey();
		}
		return null;
	}

	@Override
	public GGAPIKey getKeyForUnciphering(String realm) throws GGAPIKeyExpiredException {
		IGGAPIKeyRealm realm_ = this.keyKeeper.get().getRealm(realm);
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
		this.keyKeeper.get().createRealm(realm);
	}

	@Override
	public IGGAPIKeyRealm getRealm(String realm) throws GGAPIKeyExpiredException {
		return this.keyKeeper.get().getRealm(realm);
	}

	@Override
	public void updateRealm(IGGAPIKeyRealm realm) throws GGAPIKeyExpiredException {
		this.keyKeeper.get().update(realm);
	}

}

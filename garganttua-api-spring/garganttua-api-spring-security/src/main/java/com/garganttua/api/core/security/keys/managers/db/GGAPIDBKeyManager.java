package com.garganttua.api.core.security.keys.managers.db;

import java.util.Optional;

import com.garganttua.api.core.security.keys.GGAPIKeyManagerException;
import com.garganttua.api.core.security.keys.IGGAPIKeyManager;
import com.garganttua.api.spring.keys.domain.GGAPIKey;
import com.garganttua.api.spring.keys.domain.GGAPIKeyExpiredException;
import com.garganttua.api.spring.keys.domain.IGGAPIKeyRealm;

import lombok.Setter;

public class GGAPIDBKeyManager implements IGGAPIKeyManager {
	
	@Setter
	private Optional<IGGAPIDBKeyKeeper> keyKeeper;

	@Override
	public GGAPIKey getKeyForCiphering(String realm) throws GGAPIKeyManagerException {
		IGGAPIKeyRealm realm_;
		try {
			realm_ = this.keyKeeper.get().getRealm(realm);
			if( realm_ != null ) {
				return realm_.getCipheringKey();
			}
			return null;
		} catch (GGAPIDBKeyKeeperException | GGAPIKeyExpiredException e) {
			throw new GGAPIKeyManagerException(e);
		}
	}

	@Override
	public GGAPIKey getKeyForUnciphering(String realm) throws GGAPIKeyManagerException {
		IGGAPIKeyRealm realm_;
		try {
			realm_ = this.keyKeeper.get().getRealm(realm);
			if( realm_ != null ) {
				return realm_.getUncipheringKey();
			}
			return null;
		} catch (GGAPIDBKeyKeeperException | GGAPIKeyExpiredException e) {
			throw new GGAPIKeyManagerException(e);
		}
	}

	@Override
	public void renew(IGGAPIKeyRealm realm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createRealm(IGGAPIKeyRealm realm) throws GGAPIKeyManagerException {
		try {
			this.keyKeeper.get().createRealm(realm);
		} catch (GGAPIDBKeyKeeperException e) {
			throw new GGAPIKeyManagerException(e);
		}
	}

	@Override
	public IGGAPIKeyRealm getRealm(String realm) throws GGAPIKeyManagerException {
		try {
			return this.keyKeeper.get().getRealm(realm);
		} catch (GGAPIDBKeyKeeperException e) {
			throw new GGAPIKeyManagerException(e);
		}
	}

	@Override
	public void updateRealm(IGGAPIKeyRealm realm) throws GGAPIKeyManagerException {
		try {
			this.keyKeeper.get().update(realm);
		} catch (GGAPIDBKeyKeeperException e) {
			throw new GGAPIKeyManagerException(e);
		}
	}

}

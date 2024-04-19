package com.garganttua.api.core.security.keys.managers.inmemory;

import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.core.security.keys.GGAPIKey;
import com.garganttua.api.core.security.keys.GGAPIKeyExpiredException;
import com.garganttua.api.core.security.keys.GGAPIKeyManagerException;
import com.garganttua.api.core.security.keys.IGGAPIKeyManager;
import com.garganttua.api.core.security.keys.IGGAPIKeyRealm;

public class GGAPIInMemoryKeyManager implements IGGAPIKeyManager {

	private Map<String, IGGAPIKeyRealm> realms = new HashMap<String, IGGAPIKeyRealm>();
	
	@Override
	public GGAPIKey getKeyForCiphering(String realm) throws GGAPIKeyManagerException {
		IGGAPIKeyRealm keyRealm = this.realms.get(realm);
		try {
			return keyRealm!=null?keyRealm.getCipheringKey():null;
		} catch (GGAPIKeyExpiredException e) {
			throw new GGAPIKeyManagerException(e);
		}
	}

	@Override
	public GGAPIKey getKeyForUnciphering(String realm) throws GGAPIKeyManagerException {
		IGGAPIKeyRealm keyRealm = this.realms.get(realm);
		try {
			return keyRealm!=null?keyRealm.getUncipheringKey():null;
		} catch (GGAPIKeyExpiredException e) {
			throw new GGAPIKeyManagerException(e);
		}
	}

	@Override
	public void renew(IGGAPIKeyRealm realm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createRealm(IGGAPIKeyRealm realm) {
		this.realms.putIfAbsent(realm.getName(), realm);
	}

	@Override
	public IGGAPIKeyRealm getRealm(String realm) throws GGAPIKeyManagerException {
		return this.realms.get(realm);
	}

	@Override
	public void updateRealm(IGGAPIKeyRealm realm) throws GGAPIKeyManagerException {
		this.realms.put(realm.getName(), realm);
	}

}

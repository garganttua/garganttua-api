package com.garganttua.api.security.keys;

import java.util.HashMap;
import java.util.Map;

public class GGAPIInMemoryKeyManager implements IGGAPIKeyManager {

	private Map<String, IGGAPIKeyRealm> realms = new HashMap<String, IGGAPIKeyRealm>();
	
	@Override
	public GGAPIKey getKeyForCiphering(String realm) throws GGAPIKeyExpiredException {
		IGGAPIKeyRealm keyRealm = this.realms.get(realm);
		return keyRealm!=null?keyRealm.getCipheringKey():null;
	}

	@Override
	public GGAPIKey getKeyForUnciphering(String realm) throws GGAPIKeyExpiredException {
		IGGAPIKeyRealm keyRealm = this.realms.get(realm);
		return keyRealm!=null?keyRealm.getUncipheringKey():null;
	}

	@Override
	public void renew(IGGAPIKeyRealm realm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createRealm(IGGAPIKeyRealm realm) {
		this.realms.putIfAbsent(realm.getName(), realm);
	}

}

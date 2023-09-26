package com.garganttua.api.security.keys;

import org.springframework.beans.factory.annotation.Autowired;

public class GGAPIDBKeyManager implements IGGAPIKeyManager {
	
	@Autowired
	private IGGAPIDBKeyKeeper keyKeeper;

	@Override
	public GGAPIKey getKeyForCiphering(String realm) throws GGAPIKeyExpiredException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GGAPIKey getKeyForUnciphering(String realm) throws GGAPIKeyExpiredException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void renew(IGGAPIKeyRealm realm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createRealm(IGGAPIKeyRealm realm) {
		// TODO Auto-generated method stub
		
	}

}

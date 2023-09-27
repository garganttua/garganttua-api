package com.garganttua.api.security.keys;

public interface IGGAPIKeyManager {
	
	GGAPIKey getKeyForCiphering(String realm) throws GGAPIKeyExpiredException;
	
	GGAPIKey getKeyForUnciphering(String realm) throws GGAPIKeyExpiredException;
	
	void renew(IGGAPIKeyRealm realm);
	
	void createRealm(IGGAPIKeyRealm  realm) throws GGAPIKeyExpiredException;

}

package com.garganttua.api.security.keys;

public interface IGGAPIKeyManager {
	
	IGGAPIKeyRealm getRealm(String realm) throws GGAPIKeyManagerException;
	
	GGAPIKey getKeyForCiphering(String realm) throws GGAPIKeyManagerException;
	
	GGAPIKey getKeyForUnciphering(String realm) throws GGAPIKeyManagerException;
	
	void renew(IGGAPIKeyRealm realm);
	
	void createRealm(IGGAPIKeyRealm  realm) throws GGAPIKeyManagerException;

	void updateRealm(IGGAPIKeyRealm realm) throws GGAPIKeyManagerException;

}

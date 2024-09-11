package com.garganttua.api.core.security.keys;

import com.garganttua.api.spring.keys.domain.GGAPIKey;
import com.garganttua.api.spring.keys.domain.IGGAPIKeyRealm;

public interface IGGAPIKeyManager {
	
	IGGAPIKeyRealm getRealm(String realm) throws GGAPIKeyManagerException;
	
	GGAPIKey getKeyForCiphering(String realm) throws GGAPIKeyManagerException;
	
	GGAPIKey getKeyForUnciphering(String realm) throws GGAPIKeyManagerException;
	
	void renew(IGGAPIKeyRealm realm);
	
	void createRealm(IGGAPIKeyRealm  realm) throws GGAPIKeyManagerException;

	void updateRealm(IGGAPIKeyRealm realm) throws GGAPIKeyManagerException;

}

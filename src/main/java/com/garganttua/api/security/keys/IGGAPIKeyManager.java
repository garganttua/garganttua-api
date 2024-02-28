package com.garganttua.api.security.keys;

import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.engine.GGAPIEngineException;

public interface IGGAPIKeyManager {
	
	IGGAPIKeyRealm getRealm(String realm) throws GGAPIKeyExpiredException, GGAPIEngineException;
	
	GGAPIKey getKeyForCiphering(String realm) throws GGAPIKeyExpiredException, GGAPIEngineException;
	
	GGAPIKey getKeyForUnciphering(String realm) throws GGAPIKeyExpiredException, GGAPIEngineException;
	
	void renew(IGGAPIKeyRealm realm);
	
	void createRealm(IGGAPIKeyRealm  realm) throws GGAPIKeyExpiredException, GGAPIEntityException, GGAPIEngineException;

	void updateRealm(IGGAPIKeyRealm realm) throws GGAPIKeyExpiredException, GGAPIEntityException, GGAPIEngineException;

}

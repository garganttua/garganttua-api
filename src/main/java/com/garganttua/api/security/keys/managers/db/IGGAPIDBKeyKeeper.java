package com.garganttua.api.security.keys.managers.db;

import com.garganttua.api.core.GGAPIEntityException;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.security.keys.GGAPIKeyExpiredException;
import com.garganttua.api.security.keys.IGGAPIKeyRealm;

public interface IGGAPIDBKeyKeeper {

	IGGAPIKeyRealm getRealm(String realm) throws GGAPIEngineException;

	void createRealm(IGGAPIKeyRealm realm) throws GGAPIKeyExpiredException, GGAPIEntityException, GGAPIEngineException;

	void update(IGGAPIKeyRealm realm) throws GGAPIKeyExpiredException, GGAPIEntityException, GGAPIEngineException;
	
	void setSuperTenantId(String superTenantId);

}

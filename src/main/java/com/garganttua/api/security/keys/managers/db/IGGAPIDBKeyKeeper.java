package com.garganttua.api.security.keys.managers.db;

import com.garganttua.api.security.keys.GGAPIKeyExpiredException;
import com.garganttua.api.security.keys.IGGAPIKeyRealm;

public interface IGGAPIDBKeyKeeper {

	IGGAPIKeyRealm getRealm(String realm);

	void createRealm(IGGAPIKeyRealm realm) throws GGAPIKeyExpiredException;

	void update(IGGAPIKeyRealm realm) throws GGAPIKeyExpiredException;
	
	void setSuperTenantId(String superTenantId);

}

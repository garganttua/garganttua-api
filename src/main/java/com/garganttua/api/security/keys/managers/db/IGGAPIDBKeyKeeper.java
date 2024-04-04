package com.garganttua.api.security.keys.managers.db;

import com.garganttua.api.security.keys.IGGAPIKeyRealm;

public interface IGGAPIDBKeyKeeper {

	IGGAPIKeyRealm getRealm(String realm) throws GGAPIDBKeyKeeperException;

	void createRealm(IGGAPIKeyRealm realm) throws GGAPIDBKeyKeeperException;

	void update(IGGAPIKeyRealm realm) throws GGAPIDBKeyKeeperException;
	
	void setSuperTenantId(String superTenantId);

}

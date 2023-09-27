package com.garganttua.api.security.keys;

public interface IGGAPIDBKeyKeeper {

	IGGAPIKeyRealm getRelam(String realm);

	void createRealm(IGGAPIKeyRealm realm) throws GGAPIKeyExpiredException;

}

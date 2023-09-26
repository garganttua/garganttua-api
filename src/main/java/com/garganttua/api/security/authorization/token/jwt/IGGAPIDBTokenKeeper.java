package com.garganttua.api.security.authorization.token.jwt;

import com.garganttua.api.security.authorization.token.GGAPIToken;

public interface IGGAPIDBTokenKeeper {

	GGAPIToken findOne(GGAPIToken example);

	void store(GGAPIToken token);

}

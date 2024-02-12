package com.garganttua.api.security.authorization.tokens.jwt;

import com.garganttua.api.security.authorization.tokens.GGAPIToken;

public interface IGGAPIDBTokenKeeper {

	GGAPIToken findOne(GGAPIToken example);

	void store(GGAPIToken token);

}

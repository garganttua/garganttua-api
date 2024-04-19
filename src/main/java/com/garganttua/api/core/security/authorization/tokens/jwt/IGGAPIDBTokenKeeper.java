package com.garganttua.api.core.security.authorization.tokens.jwt;

import com.garganttua.api.core.repository.GGAPIRepositoryException;
import com.garganttua.api.core.security.authorization.tokens.GGAPIToken;

public interface IGGAPIDBTokenKeeper {

	GGAPIToken findOne(GGAPIToken example) throws GGAPIRepositoryException;

	void store(GGAPIToken token) throws GGAPIRepositoryException;

}

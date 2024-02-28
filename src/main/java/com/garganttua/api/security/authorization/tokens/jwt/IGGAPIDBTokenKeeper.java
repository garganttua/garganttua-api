package com.garganttua.api.security.authorization.tokens.jwt;

import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.security.authorization.tokens.GGAPIToken;

public interface IGGAPIDBTokenKeeper {

	GGAPIToken findOne(GGAPIToken example) throws GGAPIEngineException;

	void store(GGAPIToken token) throws GGAPIEntityException, GGAPIEngineException;

}

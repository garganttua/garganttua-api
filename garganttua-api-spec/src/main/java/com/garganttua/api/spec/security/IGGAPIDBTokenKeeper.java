package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIDBTokenKeeper {

	IGGAPIToken findOne(IGGAPIToken example) throws GGAPIException;

	void store(IGGAPIToken token) throws GGAPIException;

}

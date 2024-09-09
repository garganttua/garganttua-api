package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;

@FunctionalInterface
public interface IGGAPIAuthorizationManagerIfPresentMethod {

	void ifPresent(IGGAPIAuthorizationManager manager, IGGAPICaller caller) throws GGAPIException;
}

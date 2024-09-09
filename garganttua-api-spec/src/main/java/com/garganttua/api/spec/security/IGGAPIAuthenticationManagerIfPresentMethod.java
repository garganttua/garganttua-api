package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;

@FunctionalInterface
public interface IGGAPIAuthenticationManagerIfPresentMethod {

	void ifPresent(IGGAPIAuthenticationManager manager, IGGAPICaller caller) throws GGAPIException;
	
}

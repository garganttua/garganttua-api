package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;

@FunctionalInterface
public interface IGGAPITenantVerifierIfPresentMethod {

	void ifPresent(IGGAPITenantVerifier verifier, IGGAPICaller caller) throws GGAPIException;
	
}
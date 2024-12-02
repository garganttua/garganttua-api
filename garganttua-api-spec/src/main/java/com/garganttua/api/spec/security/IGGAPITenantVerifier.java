package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;

@FunctionalInterface
public interface IGGAPITenantVerifier {

	void verifyTenant(IGGAPICaller caller, Object authentication) throws GGAPIException;
	
}

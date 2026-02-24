package com.garganttua.api.spec.security;

import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.ApiException;

@FunctionalInterface
public interface ITenantVerifier {

	void verifyTenant(ICaller caller, Object authentication) throws ApiException;
	
}

package com.garganttua.api.commons.security;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.ApiException;

@FunctionalInterface
public interface ITenantVerifier {

	void verifyTenant(ICaller caller, Object authentication) throws ApiException;
	
}

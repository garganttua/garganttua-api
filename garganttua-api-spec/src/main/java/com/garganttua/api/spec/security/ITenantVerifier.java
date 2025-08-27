package com.garganttua.api.spec.security;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.caller.ICaller;

@FunctionalInterface
public interface ITenantVerifier {

	void verifyTenant(ICaller caller, Object authentication) throws CoreException;
	
}

package com.garganttua.api.spec.security;

import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.core.CoreException;

@FunctionalInterface
public interface ITenantVerifier {

	void verifyTenant(ICaller caller, Object authentication) throws CoreException;
	
}

package com.garganttua.api.commons.security;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.ApiException;

@FunctionalInterface
public interface IOwnerVerifier {
	
	void verifyOwner(ICaller caller, Object authentication) throws ApiException;

}

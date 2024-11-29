package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;

@FunctionalInterface
public interface IGGAPIOwnerVerifier {
	
	void verifyOwner(IGGAPICaller caller, Object authorization) throws GGAPIException;

}

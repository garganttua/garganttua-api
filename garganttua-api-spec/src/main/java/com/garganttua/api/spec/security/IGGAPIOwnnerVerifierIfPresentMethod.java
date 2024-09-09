package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;

@FunctionalInterface
public interface IGGAPIOwnnerVerifierIfPresentMethod {
	
	void ifPresent(IGGAPIOwnerVerifier verifier, IGGAPICaller caller) throws GGAPIException;

}

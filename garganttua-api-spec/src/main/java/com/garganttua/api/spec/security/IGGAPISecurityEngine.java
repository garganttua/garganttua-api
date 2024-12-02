package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInterfacesRegistry;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationServicesRegistry;

public interface IGGAPISecurityEngine {
	
	IGGAPISecurityEngine start() throws GGAPIException;

	IGGAPISecurityEngine stop() throws GGAPIException;
	
	IGGAPISecurityEngine reload() throws GGAPIException;

	IGGAPISecurityEngine flush() throws GGAPIException;

	IGGAPISecurityEngine init() throws GGAPIException;

	void verifyTenant(IGGAPICaller caller, Object authorization) throws GGAPIException;
	
	void verifyOwner(IGGAPICaller caller, Object authorization) throws GGAPIException;

	IGGAPIAuthenticationInterfacesRegistry getAuthenticationInterfacesRegistry();

	byte[] decodeAuthorizationFromRequest(Object request, IGGAPICaller caller) throws GGAPIException;

	Object decodeRawAuthorization(byte[] authorizationRaw, IGGAPICaller caller);

	IGGAPIAuthenticationServicesRegistry getAuthenticationServicesRegistry();

	boolean isStorableAuthorization(Object authorization);

}

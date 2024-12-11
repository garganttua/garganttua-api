package com.garganttua.api.spec.security;

import java.util.Map;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInterfacesRegistry;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationService;
import com.garganttua.api.spec.security.authenticator.IGGAPIAuthenticatorServicesRegistry;

public interface IGGAPISecurityEngine {
	
	IGGAPISecurityEngine start() throws GGAPIException;

	IGGAPISecurityEngine stop() throws GGAPIException;
	
	IGGAPISecurityEngine reload() throws GGAPIException;

	IGGAPISecurityEngine flush() throws GGAPIException;

	IGGAPISecurityEngine init() throws GGAPIException;

	void verifyTenant(IGGAPICaller caller, Object authentication) throws GGAPIException;
	
	void verifyOwner(IGGAPICaller caller, Object authentication) throws GGAPIException;

	IGGAPIAuthenticationInterfacesRegistry getAuthenticationInterfacesRegistry();

	byte[] decodeAuthorizationFromRequest(Object request, IGGAPICaller caller) throws GGAPIException;

	Object decodeRawAuthorization(byte[] authorizationRaw, IGGAPICaller caller);

	IGGAPIAuthenticationService getAuthenticationService();
	
	IGGAPIAuthenticatorServicesRegistry getAuthenticatorServicesRegistry();

	boolean isStorableAuthorization(Object authorization);

	void applySecurityOnAuthenticatorEntity(IGGAPICaller caller, Object entity,
			Map<String, String> params) throws GGAPIException;

}

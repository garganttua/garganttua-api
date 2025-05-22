package com.garganttua.api.spec.security;

import java.util.Map;
import java.util.Optional;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInterfacesRegistry;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationRequest;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;

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

	boolean isStorableAuthorization(Object authorization);

	void authenticatorEntitySecurityPreProcessing(IGGAPICaller caller, Object entity,
			Map<String, String> params) throws GGAPIException;
	
	void authenticatorEntitySecurityPostProcessing(IGGAPICaller caller, Object entity,
			Map<String, String> params) throws GGAPIException;

	IGGAPIServiceResponse authenticate(IGGAPIAuthenticationRequest request);

	IGGAPIAuthenticationRequest createAuthenticationRequestFromAuthorization(IGGAPICaller caller, Object authorization) throws GGAPIException;

	Optional<Object> getAuthorizationFromRequest(IGGAPICaller caller, Object request) throws GGAPIException;

}

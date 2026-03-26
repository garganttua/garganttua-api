package com.garganttua.api.spec.security;

import java.util.Map;
import java.util.Optional;

import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.security.authentication.IAuthenticationInterfacesRegistry;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.lifecycle.ILifecycle;

public interface IApiSecurityContext extends ILifecycle {

	void verifyTenant(ICaller caller, Object authentication) throws ApiException;
	
	void verifyOwner(ICaller caller, Object authentication) throws ApiException;

	IAuthenticationInterfacesRegistry getAuthenticationInterfacesRegistry();

	byte[] decodeAuthorizationFromRequest(Object request, ICaller caller) throws ApiException;

	Object decodeRawAuthorization(byte[] authorizationRaw, ICaller caller);

	boolean isStorableAuthorization(Object authorization);

	void authenticatorEntitySecurityPreProcessing(ICaller caller, Object entity,
			Map<String, String> params) throws ApiException;
	
	void authenticatorEntitySecurityPostProcessing(ICaller caller, Object entity,
			Map<String, String> params) throws ApiException;

	IAuthenticationRequest createAuthenticationRequestFromAuthorization(ICaller caller, Object authorization) throws ApiException;

	Optional<Object> getAuthorizationFromRequest(ICaller caller, Object request) throws ApiException;

}

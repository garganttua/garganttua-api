package com.garganttua.api.spec.security;

import java.util.Map;
import java.util.Optional;

import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.security.authentication.IAuthenticationInterfacesRegistry;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;
import com.garganttua.api.spec.service.IServiceResponse;
import com.garganttua.core.CoreException;
import com.garganttua.core.lifecycle.ILifecycle;

public interface IApiSecurityContext extends ILifecycle {

	void verifyTenant(ICaller caller, Object authentication) throws CoreException;
	
	void verifyOwner(ICaller caller, Object authentication) throws CoreException;

	IAuthenticationInterfacesRegistry getAuthenticationInterfacesRegistry();

	byte[] decodeAuthorizationFromRequest(Object request, ICaller caller) throws CoreException;

	Object decodeRawAuthorization(byte[] authorizationRaw, ICaller caller);

	boolean isStorableAuthorization(Object authorization);

	void authenticatorEntitySecurityPreProcessing(ICaller caller, Object entity,
			Map<String, String> params) throws CoreException;
	
	void authenticatorEntitySecurityPostProcessing(ICaller caller, Object entity,
			Map<String, String> params) throws CoreException;

	IServiceResponse authenticate(IAuthenticationRequest request);

	IAuthenticationRequest createAuthenticationRequestFromAuthorization(ICaller caller, Object authorization) throws CoreException;

	Optional<Object> getAuthorizationFromRequest(ICaller caller, Object request) throws CoreException;

}

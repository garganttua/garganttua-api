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
	
	
	
	

//	Object applySecurityOnAuthenticatorEntity(IGGAPICaller caller, Object entity) throws GGAPIException;
	
//	Object authenticate(IGGAPIAuthenticationRequest authentication) throws GGAPIException;
	
	boolean isAuthenticatorEntity(Object entity);

	void verifyTenant(IGGAPICaller caller, Object authorization) throws GGAPIException;
	
	void verifyOwner(IGGAPICaller caller, Object authorization) throws GGAPIException;

	IGGAPIAuthenticationInterfacesRegistry getAuthenticationInterfacesRegistry();

	byte[] decodeAuthorizationFromRequest(Object request, IGGAPICaller caller) throws GGAPIException;

	Object decodeRawAuthorization(byte[] authorizationRaw, IGGAPICaller caller);

	IGGAPIAuthenticationServicesRegistry getAuthenticationServicesRegistry();

//	void ifAuthorizationManagerPresent(IGGAPIAuthorizationManagerIfPresentMethod method, IGGAPICaller caller)
//			throws GGAPIException;
//
//	void ifAuthenticationProviderPresent(IGGAPIAuthenticationProviderIfPresentMethod method, IGGAPICaller caller)
//			throws GGAPIException;
	
//	void ifTenantVerifierPresent(IGGAPITenantVerifierIfPresentMethod method, IGGAPICaller caller) throws GGAPIException;
//	
//	void ifOwnerVerifierPresent(IGGAPIOwnnerVerifierIfPresentMethod method, IGGAPICaller caller) throws GGAPIException;

//	void ifAuthorizationManagerPresentOrElse(IGGAPIAuthorizationManagerIfPresentMethod method, IGGAPICaller caller, IGGAPIOrElseMethod orElseMethod)
//			throws GGAPIException;
//
//	void ifAuthenticationProviderPresentOrElse(IGGAPIAuthenticationProviderIfPresentMethod method, IGGAPICaller caller, IGGAPIOrElseMethod orElseMethod)
//			throws GGAPIException;
	
//	void ifTenantVerifierPresentOrElse(IGGAPITenantVerifierIfPresentMethod method, IGGAPICaller caller, IGGAPIOrElseMethod orElseMethod) throws GGAPIException;
//	
//	void ifOwnerVerifierPresentOrElse(IGGAPIOwnnerVerifierIfPresentMethod method, IGGAPICaller caller, IGGAPIOrElseMethod orElseMethod) throws GGAPIException;
}

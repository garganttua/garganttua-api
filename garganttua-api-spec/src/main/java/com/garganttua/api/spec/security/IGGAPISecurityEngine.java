package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;

public interface IGGAPISecurityEngine {

	Object applySecurityOnAuthenticatorEntity(Object entity) throws GGAPIException;
	
	IGGAPIAuthorization authenticate(Object Entity) throws GGAPIException;
	
	boolean isAuthenticatorEntity(Object entity);

	IGGAPIAuthorization validateAuthorization(byte[] token) throws GGAPIException;
	
	void verifyTenant(IGGAPICaller caller, IGGAPIAuthorization authorization) throws GGAPIException;
	
	void verifyOwner(IGGAPICaller caller, IGGAPIAuthorization authorization) throws GGAPIException;

	void ifAuthorizationManagerPresent(IGGAPIAuthorizationManagerIfPresentMethod method, IGGAPICaller caller)
			throws GGAPIException;

	void ifAuthenticationManagerPresent(IGGAPIAuthenticationManagerIfPresentMethod method, IGGAPICaller caller)
			throws GGAPIException;
	
	void ifTenantVerifierPresent(IGGAPITenantVerifierIfPresentMethod method, IGGAPICaller caller) throws GGAPIException;
	
	void ifOwnerVerifierPresent(IGGAPIOwnnerVerifierIfPresentMethod method, IGGAPICaller caller) throws GGAPIException;

	void ifAuthorizationManagerPresentOrElse(IGGAPIAuthorizationManagerIfPresentMethod method, IGGAPICaller caller, IGGAPIOrElseMethod orElseMethod)
			throws GGAPIException;

	void ifAuthenticationManagerPresentOrElse(IGGAPIAuthenticationManagerIfPresentMethod method, IGGAPICaller caller, IGGAPIOrElseMethod orElseMethod)
			throws GGAPIException;
	
	void ifTenantVerifierPresentOrElse(IGGAPITenantVerifierIfPresentMethod method, IGGAPICaller caller, IGGAPIOrElseMethod orElseMethod) throws GGAPIException;
	
	void ifOwnerVerifierPresentOrElse(IGGAPIOwnnerVerifierIfPresentMethod method, IGGAPICaller caller, IGGAPIOrElseMethod orElseMethod) throws GGAPIException;
}

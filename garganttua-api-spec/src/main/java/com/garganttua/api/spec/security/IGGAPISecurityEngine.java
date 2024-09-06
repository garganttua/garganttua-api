package com.garganttua.api.spec.security;

import java.util.List;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.engine.IGGAPIAccessRulesRegistry;

public interface IGGAPISecurityEngine {

	IGGAPIAccessRulesRegistry getAccessRulesRegistry();
	
	Object applySecurityOnAuthenticatorEntity(Object entity) throws GGAPIException;

	List<String> getAuthorities();
	
	IGGAPIAuthorization authenticate(Object Entity) throws GGAPIException;
	
	boolean isAuthenticatorEntity(Object entity);

	IGGAPIAuthorization validateAuthorization(byte[] token) throws GGAPIException;
	
	void verifyTenant(IGGAPICaller caller, IGGAPIAuthorization authorization) throws GGAPIException;
	
	void verifyOwner(IGGAPICaller caller, IGGAPIAuthorization authorization) throws GGAPIException;


}

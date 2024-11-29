package com.garganttua.api.spec.security.authentication;

import com.garganttua.api.spec.domain.IGGAPIDomain;

public interface IGGAPIAuthenticationRequest {

	IGGAPIDomain getDomain();

	String getTenantId();

	String getPrincipal();

	Object getCredentials();
	
	Class<?> getAuthenticationType();

}

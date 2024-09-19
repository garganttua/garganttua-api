package com.garganttua.api.spec.security;

import java.util.List;

public interface IGGAPIAuthentication {

	String getTenantId();
	
	Object getPrincipal();
	
	boolean isAuthenticated();
	
	IGGAPIAuthorization getAuthorization();
	
	void setAuthorization(IGGAPIAuthorization authorization);

	List<String> getAuthoritieList();

	IGGAPIAuthenticator getAuthenticator();
	
}

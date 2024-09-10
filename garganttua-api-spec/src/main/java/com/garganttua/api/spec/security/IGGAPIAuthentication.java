package com.garganttua.api.spec.security;

import java.util.List;

public interface IGGAPIAuthentication {

	Object getPrincipal();
	
	boolean isAuthenticated();
	
	IGGAPIAuthorization getAuthorization();

	List<String> getAuthorities();
	
}

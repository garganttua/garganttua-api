package com.garganttua.api.spec.security;

import java.util.List;

public interface IGGAPIAuthenticator/* extends UserDetails */{
	
	String getUuid();
	
	String getTenantId(); 
	
	Object getEntity();

	IGGAPIAuthentication getAuthentication();

	void setAuthorities(List<String> authorities);

}

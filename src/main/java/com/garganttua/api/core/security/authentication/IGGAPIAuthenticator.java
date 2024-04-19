package com.garganttua.api.core.security.authentication;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public interface IGGAPIAuthenticator extends UserDetails {
	
	String getUuid();
	
	String getTenantId(); 
	
	Object getEntity();

	Authentication getAuthentication();

	void setAuthorities(List<String> authorities);

}

package com.garganttua.api.security.authentication;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.garganttua.api.core.IGGAPIEntity;

public interface IGGAPIAuthenticator {
	
	String getUuid();
	
	String getTenantId(); 
	
	IGGAPIEntity getEntity();

	Authentication getAuthentication();

	void setAuthorities(List<String> authorities);

}

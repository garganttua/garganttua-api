package com.garganttua.api.security.authentication;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.garganttua.api.core.IGGAPIEntity;

public interface IGGAPIAuthenticator extends UserDetails, IGGAPIEntity {
	
	String getUuid();
	
	String getTenantId(); 

	Authentication getAuthentication();
	
	@JsonIgnore
	Collection<? extends GrantedAuthority> getAuthorities();
}

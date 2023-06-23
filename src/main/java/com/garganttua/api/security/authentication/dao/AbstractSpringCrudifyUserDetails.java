package com.garganttua.api.security.authentication.dao;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;

public class AbstractSpringCrudifyUserDetails implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = 884452902945167964L;
	
	@Getter
	private final String username;
	
	@Getter
	private final boolean enabled;
	
	@Getter
	private Collection<? extends GrantedAuthority> authorities;
	
	@Getter
	private final String password;
	
	@Getter
	private final String tenantId;
	
	@Getter
	private final String uuid;
	
	public AbstractSpringCrudifyUserDetails(final String username, final String uuid, final boolean enabled, final String password, final String tenantId, Collection<? extends GrantedAuthority> authorities) {
		this.username = username;
		this.password = password;
		this.enabled = enabled;
		this.tenantId = tenantId; 
		this.authorities = authorities;
		this.uuid = uuid;		
	}
	

	@Override
	public boolean isAccountNonExpired() {
		return this.enabled;
	}


	@Override
	public boolean isAccountNonLocked() {
		return this.enabled;
	}


	@Override
	public boolean isCredentialsNonExpired() {
		return this.enabled;
	}

}

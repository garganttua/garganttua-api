package com.garganttua.api.security.authentication.dao;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.Setter;

public class AbstractGGAPIUserDetails implements UserDetails, IGGAPIUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 884452902945167964L;
	
	@Getter
	@Setter
	private String username;
	
	@Getter
	@Setter
	private boolean enabled;
	
	@Getter
	@Setter
	private Collection<? extends GrantedAuthority> authorities;
	
	@Getter
	@Setter
	private String password;
	
	@Getter
	@Setter
	private String tenantId;
	
	@Getter
	@Setter
	private String uuid;
	
	public AbstractGGAPIUserDetails(final String username, final String uuid, final boolean enabled, final String password, final String tenantId, Collection<? extends GrantedAuthority> authorities) {
		this.username = username;
		this.password = password;
		this.enabled = enabled;
		this.tenantId = tenantId; 
		
		if( authorities == null ) {
			this.authorities = new ArrayList<GrantedAuthority>();
		} else {
			this.authorities = authorities;
		}
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

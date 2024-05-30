package com.garganttua.api.core.security.authentication.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.garganttua.api.spec.security.IGGAPIAuthenticator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class GGAPIEntityAuthenticator implements IGGAPIAuthenticator {

	private static final long serialVersionUID = -2761843271829076643L;
	
	private String password;
	private List<String> authorities;
	private String userName;
	private boolean accountNonExpired;
	private boolean accountNonLocked;
	private boolean credentialsNonExpired;
	private boolean enabled;
	private String uuid;
	private String tenantId;
	@Getter
	private Object entity;
	
	@Setter
	private GGAPIEntityAuthentication authentication;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		if( this.authorities != null ) {
			this.authorities.forEach(auth -> {
				authorities.add(new GrantedAuthority() {
				
					private static final long serialVersionUID = -8283536681606086962L;
	
					@Override
					public String getAuthority() {
						return auth;
					}
				});
			});
		}
		return authorities;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.userName;
	}

	@Override
	public boolean isAccountNonExpired() {
		return this.accountNonExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.credentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	@Override
	public String getUuid() {
		return this.uuid;
	}

	@Override
	public String getTenantId() {
		return this.tenantId;
	}

	@Override
	public Authentication getAuthentication() {
		return this.authentication;
	}

	@Override
	public void setAuthorities(List<String> authorities) {
		this.authorities = authorities;	
	}

}

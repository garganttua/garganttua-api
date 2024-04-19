package com.garganttua.api.core.security.authentication.entity;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class GGAPIEntityAuthentication implements Authentication {

	private static final long serialVersionUID = -4068108823963678087L;
	private GGAPIEntityAuthenticator authenticator;
	private boolean authenticated = false;

	public GGAPIEntityAuthentication(GGAPIEntityAuthenticator authenticator) {
		this.authenticator = authenticator;
		this.authenticator.setAuthentication(this);
	}

	@Override
	public String getName() {
		return this.authenticator.getUsername();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authenticator.getAuthorities();
	}

	@Override
	public Object getCredentials() {
		return this.authenticator.getPassword();
	}

	@Override
	public Object getDetails() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return this.authenticator;
	}

	@Override
	public boolean isAuthenticated() {
		return this.authenticated ;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		this.authenticated = isAuthenticated;
	}

}

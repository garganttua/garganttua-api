package com.garganttua.api.security.spring.core.authentication;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.garganttua.api.core.security.authentication.GGAPIAuthenticationHelper;
import com.garganttua.api.spec.GGAPIException;

public class GGAPISpringAuthentication implements Authentication {

	private static final long serialVersionUID = 4650365846780785344L;
	
	private Object authentication;

	public GGAPISpringAuthentication(Object authentication) {
		this.authentication = authentication;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		try {
			return GGAPIAuthenticationHelper.getAuthorities(this.authentication).stream().map(authority -> {
				return new GrantedAuthority() {
					private static final long serialVersionUID = -196730545005864762L;
					@Override
					public String getAuthority() {
						return authority;
					}
				};
				
			}).collect(Collectors.toCollection(null));
		} catch (GGAPIException e) {
			return null;
		}
	}

	@Override
	public Object getCredentials() {
		try {
			return GGAPIAuthenticationHelper.getCredentials(this.authentication);
		} catch (GGAPIException e) {
			return null;
		}
	}

	@Override
	public Object getDetails() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPrincipal() {
		try {
			return GGAPIAuthenticationHelper.getPrincipal(this.authentication);
		} catch (GGAPIException e) {
			return null;
		}
	}

	@Override
	public boolean isAuthenticated() {
		try {
			return GGAPIAuthenticationHelper.isAuthenticated(this.authentication);
		} catch (GGAPIException e) {
			return false;
		}
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		try {
			GGAPIAuthenticationHelper.setAuthenticated(this.authentication, isAuthenticated);
		} catch (GGAPIException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public Object getAuthentication() {
		return this.authentication;
	}

}

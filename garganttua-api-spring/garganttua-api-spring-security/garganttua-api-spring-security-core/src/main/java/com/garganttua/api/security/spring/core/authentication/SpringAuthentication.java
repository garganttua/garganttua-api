package com.garganttua.api.security.spring.core.authentication;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.garganttua.api.core.security.authentication.AuthenticationHelper;
import com.garganttua.api.spec.CoreException;

import lombok.Getter;

public class SpringAuthentication implements Authentication {

	private static final long serialVersionUID = 4650365846780785344L;
	
	@Getter
	private Object authentication;

	public SpringAuthentication(Object authentication) {
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
			return AuthenticationHelper.getAuthorities(this.authentication).stream().map(authority -> {
				return new GrantedAuthority() {
					private static final long serialVersionUID = -196730545005864762L;
					@Override
					public String getAuthority() {
						return authority;
					}
				};
				
			}).collect(Collectors.toList());
		} catch (CoreException e) {
			return null;
		}
	}

	@Override
	public Object getCredentials() {
		try {
			return AuthenticationHelper.getCredentials(this.authentication);
		} catch (CoreException e) {
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
			return AuthenticationHelper.getPrincipal(this.authentication);
		} catch (CoreException e) {
			return null;
		}
	}

	@Override
	public boolean isAuthenticated() {
		try {
			return AuthenticationHelper.isAuthenticated(this.authentication);
		} catch (CoreException e) {
			return false;
		}
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		try {
			AuthenticationHelper.setAuthenticated(this.authentication, isAuthenticated);
		} catch (CoreException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public Object getAuthentication() {
		return this.authentication;
	}

	public String getTenantId() {
		try {
			return AuthenticationHelper.getTenantId(this.authentication);
		} catch (CoreException e) {
			return null;
		}
	}
	
	public String getOwnerId() {
		try {
			return AuthenticationHelper.getOwnerId(this.authentication);
		} catch (CoreException e) {
			return null;
		}
	}

}

package com.garganttua.api.security.spring.core.authentication;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.garganttua.api.core.security.authentication.AuthenticationHelper;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;

public class SpringAuthenticationRequest implements Authentication {

	private static final long serialVersionUID = -2144851572357335188L;
	private Object principal;
	private IAuthenticationRequest credentials;
	private boolean authenticated = false;
	private Object authenticationResponse = null;

	public SpringAuthenticationRequest(IAuthenticationRequest request) {
		this.principal = request.getPrincipal();
		this.credentials = request;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		if (this.authenticationResponse == null) {
			return Collections.emptyList();
		} else {
			try {
				return AuthenticationHelper.getAuthorities(this.authenticationResponse).stream().map(authority -> {
					return new GrantedAuthority() {
						
						private static final long serialVersionUID = -7948612210245867613L;

						@Override
						public String getAuthority() {
							// TODO Auto-generated method stub
							return authority;
						}
					};
				}).collect(Collectors.toList());
			} catch (CoreException e) {
				return null;
			}
		}
	}

	@Override
	public Object getCredentials() {
		if (this.authenticationResponse == null) {
			return this.credentials;
		} else {
			try {
				return AuthenticationHelper.getCredentials(this.authenticationResponse);
			} catch (CoreException e) {
				return null;
			}
		}
	}

	@Override
	public Object getDetails() {
		return this.credentials;
	}

	@Override
	public Object getPrincipal() {
		if (this.authenticationResponse == null) {
			return this.principal;
		} else {
			try {
				return AuthenticationHelper.getPrincipal(this.authenticationResponse);
			} catch (CoreException e) {
				return null;
			}
		}
	}

	@Override
	public boolean isAuthenticated() {
		if (this.authenticationResponse == null) {
			return this.authenticated;
		} else {
			try {
				return AuthenticationHelper.isAuthenticated(this.authenticationResponse);
			} catch (CoreException e) {
				return false;
			}
		}
	}
	
	public Object getAuthorization() throws CoreException {
		if (this.authenticationResponse == null) {
			return null;
		} else {
			return AuthenticationHelper.getAuthorization(this.authenticationResponse);
		}
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		this.authenticated = isAuthenticated;
	}

	public void setAuthenticationFromEngine(Object authenticationResponse) {
		this.authenticationResponse = authenticationResponse;
	}
}

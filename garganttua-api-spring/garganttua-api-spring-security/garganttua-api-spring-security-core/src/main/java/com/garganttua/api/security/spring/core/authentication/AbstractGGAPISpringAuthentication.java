package com.garganttua.api.security.spring.core.authentication;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;

import com.garganttua.api.spec.security.IGGAPIAuthentication;
import com.garganttua.api.spec.security.IGGAPIAuthenticator;
import com.garganttua.api.spec.security.IGGAPIAuthorization;

import lombok.Setter;

public abstract class AbstractGGAPISpringAuthentication implements IGGAPIAuthentication {
	
	private Authentication authentication;
	@Setter
	private IGGAPIAuthorization authorization;
	
	public abstract Authentication toSpringAuthentication();

	@Override
	public Object getPrincipal() {
		return this.authentication.getPrincipal();
	}

	@Override
	public boolean isAuthenticated() {
		return this.authentication.isAuthenticated();
	}
	

	@Override
	public List<String> getAuthorities() {
		return this.authentication.getAuthorities().stream().map((auth) -> {
			return auth.getAuthority();
		}).collect(Collectors.toList());
	}

	@Override
	public IGGAPIAuthorization getAuthorization() {
		return this.authorization;
	}

	public void setAuthenticated(Authentication authentication) {
		this.authentication = authentication;
	}

	@Override
	public IGGAPIAuthenticator getAuthenticator() {
		return (IGGAPIAuthenticator) this.authentication.getPrincipal();
	}

}

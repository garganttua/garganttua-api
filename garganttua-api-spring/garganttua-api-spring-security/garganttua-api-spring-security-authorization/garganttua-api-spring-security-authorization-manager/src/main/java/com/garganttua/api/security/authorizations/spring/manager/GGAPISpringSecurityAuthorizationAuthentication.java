package com.garganttua.api.security.authorizations.spring.manager;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.garganttua.api.security.spring.core.authentication.IGGAPISpringAuthentication;
import com.garganttua.api.spec.security.IGGAPIAuthenticator;
import com.garganttua.api.spec.security.IGGAPIAuthorization;

import lombok.Getter;

public class GGAPISpringSecurityAuthorizationAuthentication implements IGGAPISpringAuthentication {

	private static final long serialVersionUID = 6973977239085301281L;
	private IGGAPIAuthorization authorization;
	private boolean authenticated = false;
	@Getter
	private String tenantId;

	private GGAPISpringSecurityAuthorizationAuthentication(IGGAPIAuthorization authorization) {
		this.authorization = authorization;
		this.authenticated = true;
		this.tenantId = authorization.getTenantId();
	}

	public static GGAPISpringSecurityAuthorizationAuthentication fromAuthorization(IGGAPIAuthorization authorization) {
		return new GGAPISpringSecurityAuthorizationAuthentication(authorization);
	}

	@Override
	public Object getPrincipal() {
		return this.authorization.getOwnerId();
	}

	@Override
	public boolean isAuthenticated() {
		return this.authenticated;
	}

	@Override
	public IGGAPIAuthorization getAuthorization() {
		return this.authorization;
	}

	@Override
	public void setAuthorization(IGGAPIAuthorization authorization) {
		
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorization.getAuthorities().stream().map((authority) -> {
			return new GrantedAuthority() {
				
				private static final long serialVersionUID = 8560809286948459803L;

				@Override
				public String getAuthority() {
					return authority;
				}
			};
		}).collect(Collectors.toList());
		
	}

	@Override
	public IGGAPIAuthenticator getAuthenticator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getCredentials() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getDetails() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAuthenticated(Authentication authentication) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getAuthoritieList() {
		return (List<String>) this.authorization.getAuthorities();
	}

}

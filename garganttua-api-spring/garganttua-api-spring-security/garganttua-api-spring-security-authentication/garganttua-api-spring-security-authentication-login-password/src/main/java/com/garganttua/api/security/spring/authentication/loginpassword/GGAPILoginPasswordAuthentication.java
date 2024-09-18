package com.garganttua.api.security.spring.authentication.loginpassword;

import java.util.List;import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.garganttua.api.security.spring.core.authentication.IGGAPISpringAuthentication;
import com.garganttua.api.spec.security.IGGAPIAuthenticator;
import com.garganttua.api.spec.security.IGGAPIAuthorization;

import lombok.Getter;

public class GGAPILoginPasswordAuthentication extends UsernamePasswordAuthenticationToken implements IGGAPISpringAuthentication {
	
	private static final long serialVersionUID = -1601357056119543229L;
	private IGGAPIAuthorization authorization;
	@Getter
	private boolean authenticated = false;
	private Object login;
	private String password;

	private GGAPILoginPasswordAuthentication(String login, String password) {
		super(login, password);
		this.login = login;
		this.password = password; 
	}

	public static GGAPILoginPasswordAuthentication fromRequest(GGAPILoginPasswordAuthenticationRequest request) {
		return new GGAPILoginPasswordAuthentication(request.getLogin(), request.getPassword());
	}
	
	@Override
	public IGGAPIAuthorization getAuthorization() {
		return this.authorization;
	}

	@Override
	public IGGAPIAuthenticator getAuthenticator() {
		return (IGGAPIAuthenticator) this.getPrincipal();
	}
	
	public void setAuthenticated(Authentication authentication) throws IllegalArgumentException {
		this.authenticated  = authentication.isAuthenticated();
		this.login = authentication.getPrincipal();
	}

	@Override
	public Object getPrincipal() {
		return this.login;
	}

	@Override
	public Object getCredentials() {
		return this.password;
	}

	@Override
	public void setAuthorization(IGGAPIAuthorization authorization) {
		this.authorization = authorization;
	}

	@Override
	public List<String> getAuthoritieList() {
		return this.getAuthorities().stream().map((authority) -> {
			return authority.getAuthority();
		}).collect(Collectors.toList());
	}

}

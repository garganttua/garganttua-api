package com.garganttua.api.security.spring.authentication.loginpassword;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.garganttua.api.spec.security.IGGAPIAuthentication;
import com.garganttua.api.spec.security.IGGAPIAuthorization;

public class GGAPILoginPasswordAuthentication implements IGGAPIAuthentication {
	
	private String login;
	
	private String password;
	
	private GGAPILoginPasswordAuthentication(String login, String password) {
		this.login = login;
		this.password = password;
	}

	public static GGAPILoginPasswordAuthentication fromRequest(GGAPILoginPasswordAuthenticationRequest request) {
		return new GGAPILoginPasswordAuthentication(request.getLogin(), request.getPassword());
	}

	private Authentication authentication;

	public Authentication toSpringAuthentication() {
		return new UsernamePasswordAuthenticationToken(this.login, this.password);
	}

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
		return null;
	}

	public void setAuthenticated(Authentication authentication) {
		this.authentication = authentication;
	}
}

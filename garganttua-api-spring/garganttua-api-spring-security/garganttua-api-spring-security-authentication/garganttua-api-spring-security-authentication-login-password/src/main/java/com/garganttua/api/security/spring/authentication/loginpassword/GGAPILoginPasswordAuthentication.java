package com.garganttua.api.security.spring.authentication.loginpassword;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.garganttua.api.security.spring.core.authentication.AbstractGGAPISpringAuthentication;

public class GGAPILoginPasswordAuthentication extends AbstractGGAPISpringAuthentication {
	
	private String login;
	
	private String password;

	private GGAPILoginPasswordAuthentication(String login, String password) {
		this.login = login;
		this.password = password;
	}

	public static GGAPILoginPasswordAuthentication fromRequest(GGAPILoginPasswordAuthenticationRequest request) {
		return new GGAPILoginPasswordAuthentication(request.getLogin(), request.getPassword());
	}
	
	public Authentication toSpringAuthentication() {
		return new UsernamePasswordAuthenticationToken(this.login, this.password);
	}

}

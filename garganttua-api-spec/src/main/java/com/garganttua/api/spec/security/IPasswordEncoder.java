package com.garganttua.api.spec.security;

public interface IPasswordEncoder {

	String encode(String password);
	
	boolean matches(String rawPassword, String encodedPassword);
	
}

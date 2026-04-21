package com.garganttua.api.commons.security;

public interface IPasswordEncoder {

	String encode(String password);
	
	boolean matches(String rawPassword, String encodedPassword);
	
}

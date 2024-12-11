package com.garganttua.api.spec.security;

public interface IGGAPIPasswordEncoder {

	String encode(String password);
	
	boolean matches(String rawPassword, String encodedPassword);
	
}

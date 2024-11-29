package com.garganttua.api.core.security.authentication.loginpassword;

public interface IGGAPIPasswordEncoder {

	String encode(String password);
	
	boolean matches(String rawPassword, String encodedPassword);
	
}

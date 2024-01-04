package com.garganttua.api.security.authentication.modes.loginpassword;

import com.garganttua.api.security.authentication.IGGAPIAuthenticator;

public interface IGGAPILoginPasswordAuthenticationEntity extends IGGAPIAuthenticator {
	
	String getLogin();
	
	String getPassword();
	
}

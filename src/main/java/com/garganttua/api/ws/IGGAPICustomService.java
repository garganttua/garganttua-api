package com.garganttua.api.ws;

import com.garganttua.api.security.authorization.IGGAPIAccessRule;

public interface IGGAPICustomService {

	String getMethodName();

	Class<?>[] getParameters();
	
	String getPath();
	
	String getAuthority();
	
	IGGAPIAccessRule getAccessRule();

	String getDescription();

}

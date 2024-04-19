package com.garganttua.api.core.service;

import com.garganttua.api.core.security.authorization.IGGAPIAccessRule;

public interface IGGAPICustomService {

	String getMethodName();

	Class<?>[] getParameters();
	
	String getPath();
	
	String getAuthority();
	
	IGGAPIAccessRule getAccessRule();

	String getDescription();

}

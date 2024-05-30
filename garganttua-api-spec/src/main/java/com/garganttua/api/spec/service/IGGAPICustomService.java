package com.garganttua.api.spec.service;

import com.garganttua.api.spec.security.IGGAPIAccessRule;

public interface IGGAPICustomService {

	String getMethodName();

	Class<?>[] getParameters();
	
	String getPath();
	
	String getAuthority();
	
	IGGAPIAccessRule getAccessRule();

	String getDescription();

}

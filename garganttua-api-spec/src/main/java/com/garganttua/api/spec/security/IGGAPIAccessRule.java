package com.garganttua.api.spec.security;

import com.garganttua.api.spec.service.GGAPIServiceAccess;
import com.garganttua.api.spec.service.GGAPIServiceMethod;

public interface IGGAPIAccessRule {
	
	String getEndpoint();
	
	String getAuthority();
	
	GGAPIServiceMethod getMethod();
	
	String toString();
	
	GGAPIServiceAccess getAccess();
}

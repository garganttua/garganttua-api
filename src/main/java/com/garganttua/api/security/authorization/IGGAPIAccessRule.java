package com.garganttua.api.security.authorization;

import com.garganttua.api.core.GGAPIServiceAccess;
import com.garganttua.api.service.GGAPIServiceMethod;

public interface IGGAPIAccessRule {
	
	String getEndpoint();
	
	String getAuthority();
	
	GGAPIServiceMethod getMethod();
	
	String toString();
	
	GGAPIServiceAccess getAccess();
}

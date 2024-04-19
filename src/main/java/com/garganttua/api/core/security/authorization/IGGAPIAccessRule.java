package com.garganttua.api.core.security.authorization;

import com.garganttua.api.core.GGAPIServiceAccess;
import com.garganttua.api.core.service.GGAPIServiceMethod;

public interface IGGAPIAccessRule {
	
	String getEndpoint();
	
	String getAuthority();
	
	GGAPIServiceMethod getMethod();
	
	String toString();
	
	GGAPIServiceAccess getAccess();
}

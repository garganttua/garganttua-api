package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

public interface IGGAPIAccessRule {
	
	String getEndpoint();
	
	String getAuthority();
	
	GGAPIEntityOperation getOperation();
	
	String toString();
	
	GGAPIServiceAccess getAccess();
}

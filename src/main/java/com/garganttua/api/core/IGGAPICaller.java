package com.garganttua.api.core;

import java.util.List;

import com.garganttua.api.core.engine.GGAPIDomain;
import com.garganttua.api.core.security.authorization.IGGAPIAccessRule;

public interface IGGAPICaller {
	
	String getTenantId();
	
	String getRequestedTenantId();
	
	String getOwnerId();
	
	boolean isSuperTenant();
	
	boolean isSuperOwner();
	
	GGAPIDomain getDomain();

	IGGAPIAccessRule getAccessRule();
	
	boolean isAnonymous();
	
	List<String> getAuthorities();
}

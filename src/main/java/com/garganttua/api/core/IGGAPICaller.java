package com.garganttua.api.core;

import java.util.List;

import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.security.authorization.IGGAPIAccessRule;

public interface IGGAPICaller {
	
	String getTenantId();
	
	String getRequestedTenantId();
	
	String getOwnerId();
	
	boolean isSuperTenant();
	
	boolean isSuperOwner();
	
	GGAPIDynamicDomain getDomain();

	IGGAPIAccessRule getAccessRule();
	
	boolean isAnonymous();
	
	List<String> getAuthorities();
}

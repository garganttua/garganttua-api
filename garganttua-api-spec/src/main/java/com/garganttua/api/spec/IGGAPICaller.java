package com.garganttua.api.spec;

import java.util.List;

import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.domain.IGGAPIDomain;

public interface IGGAPICaller {
	
	String getTenantId();
	
	String getRequestedTenantId();
	
	String getOwnerId();
	
	boolean isSuperTenant();
	
	boolean isSuperOwner();
	
	IGGAPIDomain getDomain();

	IGGAPIAccessRule getAccessRule();
	
	boolean isAnonymous();
	
	List<String> getAuthorities();

	void deleteTenantId();

	void deleteRequestedTenantId();
}

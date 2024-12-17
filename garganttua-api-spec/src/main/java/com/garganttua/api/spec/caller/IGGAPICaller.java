package com.garganttua.api.spec.caller;

import java.util.List;

import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.domain.IGGAPIDomain;

public interface IGGAPICaller {
	
	String getTenantId();
	
	String getRequestedTenantId();
	
	String getOwnerId();
	
	String getCallerId();
	
	boolean isSuperTenant();
	
	boolean isSuperOwner();
	
	IGGAPIDomain getDomain();

	IGGAPIAccessRule getAccessRule();
	
	boolean isAnonymous();
	
	List<String> getAuthorities();

  void setOwnerId(String ownerId);

}

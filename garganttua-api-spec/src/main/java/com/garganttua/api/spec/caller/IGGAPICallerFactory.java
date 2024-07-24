package com.garganttua.api.spec.caller;

import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIException;

public interface IGGAPICallerFactory {
	
	IGGAPICaller getCaller(GGAPIEntityOperation operation, String endpoint, String tenantId, String ownerId, String requestedTenantId, String callerId) throws GGAPIException ;

}

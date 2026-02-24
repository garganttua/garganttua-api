package com.garganttua.api.spec.caller;

import com.garganttua.api.spec.context.Operation;
import com.garganttua.api.spec.ApiException;

public interface ICallerFactory {
	
	ICaller getCaller(Operation operation, String endpoint, String tenantId, String ownerId, String requestedTenantId, String callerId) throws ApiException ;

}

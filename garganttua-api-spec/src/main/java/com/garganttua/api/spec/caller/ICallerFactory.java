package com.garganttua.api.spec.caller;

import com.garganttua.api.spec.operation.Operation;
import com.garganttua.api.spec.ApiException;

public interface ICallerFactory {
	
	ICaller getCaller(Operation operation, String endpoint, String tenantId, String ownerId, String requestedTenantId, String callerId) throws ApiException ;

}

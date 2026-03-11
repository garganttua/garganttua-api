package com.garganttua.api.spec.caller;

import com.garganttua.api.spec.operation.OperationDefinition;
import com.garganttua.api.spec.ApiException;

public interface ICallerFactory {
	
	ICaller getCaller(OperationDefinition operation, String endpoint, String tenantId, String ownerId, String requestedTenantId, String callerId) throws ApiException ;

}

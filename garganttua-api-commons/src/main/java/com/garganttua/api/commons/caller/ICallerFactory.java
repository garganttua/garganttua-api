package com.garganttua.api.commons.caller;

import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.ApiException;

public interface ICallerFactory {
	
	ICaller getCaller(OperationDefinition operation, String endpoint, String tenantId, String ownerId, String requestedTenantId, String callerId) throws ApiException ;

}

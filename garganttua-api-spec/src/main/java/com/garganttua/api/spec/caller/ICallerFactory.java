package com.garganttua.api.spec.caller;

import com.garganttua.api.spec.context.Operation;
import com.garganttua.core.CoreException;

public interface ICallerFactory {
	
	ICaller getCaller(Operation operation, String endpoint, String tenantId, String ownerId, String requestedTenantId, String callerId) throws CoreException ;

}

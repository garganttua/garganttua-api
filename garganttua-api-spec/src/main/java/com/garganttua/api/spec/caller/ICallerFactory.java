package com.garganttua.api.spec.caller;

import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.CoreException;

public interface ICallerFactory {
	
	ICaller getCaller(EntityOperation operation, String endpoint, String tenantId, String ownerId, String requestedTenantId, String callerId) throws CoreException ;

}

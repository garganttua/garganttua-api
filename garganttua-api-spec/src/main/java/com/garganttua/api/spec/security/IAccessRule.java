package com.garganttua.api.spec.security;

import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.service.ServiceAccess;

public interface IAccessRule {
	
	String getEndpoint();
	
	String getAuthority();
	
	EntityOperation getOperation();
	
	String toString();
	
	ServiceAccess getAccess();
}

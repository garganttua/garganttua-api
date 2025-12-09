package com.garganttua.api.spec.security;

import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.Operation;

public interface IAccessRule {
	
	String getEndpoint();
	
	String getAuthority();
	
	Operation getOperation();
	
	String toString();
	
	Access getAccess();
}

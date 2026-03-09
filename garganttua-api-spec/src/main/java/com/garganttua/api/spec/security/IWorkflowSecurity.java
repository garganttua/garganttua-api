package com.garganttua.api.spec.security;

import com.garganttua.api.spec.operation.Access;

public interface IWorkflowSecurity {

	boolean isDisabled();

	boolean hasAuthority();

	Access getAccess();

}

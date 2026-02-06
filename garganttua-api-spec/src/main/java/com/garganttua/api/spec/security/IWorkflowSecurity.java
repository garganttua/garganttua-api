package com.garganttua.api.spec.security;

import com.garganttua.api.spec.context.Access;

public interface IWorkflowSecurity {

	boolean isDisabled();

	boolean hasAuthority();

	Access getAccess();

}

package com.garganttua.api.commons.security;

import com.garganttua.api.commons.operation.Access;

public interface IWorkflowSecurity {

	boolean isDisabled();

	boolean hasAuthority();

	Access getAccess();

}

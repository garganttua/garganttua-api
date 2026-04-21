package com.garganttua.api.commons.definition;

import com.garganttua.core.reflection.binders.IMethodBinder;

public interface IAuthenticationDefinition {

    IMethodBinder<?> authenticateMethodBinder();

}

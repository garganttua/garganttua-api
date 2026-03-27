package com.garganttua.api.spec.definition;

import com.garganttua.core.reflection.binders.IMethodBinder;

public interface IAuthenticationDefinition {

    IMethodBinder<?> authenticateMethodBinder();

}

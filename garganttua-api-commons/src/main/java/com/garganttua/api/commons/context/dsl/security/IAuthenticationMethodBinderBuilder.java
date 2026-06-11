package com.garganttua.api.commons.context.dsl.security;

import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.IMethodBinderBuilder;

public interface IAuthenticationMethodBinderBuilder<ExecutionReturn> extends IMethodBinderBuilder<ExecutionReturn, IAuthenticationMethodBinderBuilder<ExecutionReturn>, IAuthenticationBuilder<?>, IMethodBinder<ExecutionReturn>>{

	String getAuthenticateMethodName();

}

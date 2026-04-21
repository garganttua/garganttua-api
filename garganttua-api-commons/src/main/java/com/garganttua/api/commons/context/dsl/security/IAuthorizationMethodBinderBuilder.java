package com.garganttua.api.commons.context.dsl.security;

import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.IMethodBinderBuilder;


public interface IAuthorizationMethodBinderBuilder<E> extends IMethodBinderBuilder<Object, IAuthorizationMethodBinderBuilder<E>, IAuthorizationBuilder<E>, IMethodBinder<Object>> {
}

package com.garganttua.api.spec.context.dsl;

import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.IMethodBinderBuilder;

public interface IDomainStartupBinderBuilder<E> extends IMethodBinderBuilder<Void, IDomainStartupBinderBuilder<E>, IDomainBuilder<E>, IMethodBinder<Void>> {

}

package com.garganttua.api.spec.context.dsl;

import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.IMethodBinderBuilder;

public interface IEntityMethodBinderBuilder<E> extends IMethodBinderBuilder<Void, IEntityMethodBinderBuilder<E>, IEntityBuilder<E>, IMethodBinder<Void>>{

}

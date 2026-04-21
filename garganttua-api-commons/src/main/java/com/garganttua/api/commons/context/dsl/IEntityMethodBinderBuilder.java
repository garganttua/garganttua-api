package com.garganttua.api.commons.context.dsl;

import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.IMethodBinderBuilder;

public interface IEntityMethodBinderBuilder<E> extends IMethodBinderBuilder<Void, IEntityMethodBinderBuilder<E>, IEntityBuilder<E>, IMethodBinder<Void>>{

}

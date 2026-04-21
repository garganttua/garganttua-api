package com.garganttua.api.commons.context.dsl;

import com.garganttua.api.commons.context.IUseCase;
import com.garganttua.core.reflection.binders.dsl.IMethodBinderBuilder;

public interface IUseCaseBinderBuilder<I, O, E> extends IMethodBinderBuilder<O, IUseCaseBinderBuilder<I, O, E>, IUseCaseBuilder<I, O, E>, IUseCase<I,O>> {

    String getMethodName();

}

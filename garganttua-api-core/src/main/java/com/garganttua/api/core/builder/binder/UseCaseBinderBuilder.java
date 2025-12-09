package com.garganttua.api.core.builder.binder;

import com.garganttua.api.spec.context.IUseCase;
import com.garganttua.api.spec.context.dsl.IUseCaseBinderBuilder;
import com.garganttua.api.spec.context.dsl.IUseCaseBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.IObjectSupplier;
import com.garganttua.core.supply.dsl.IObjectSupplierBuilder;

public class UseCaseBinderBuilder<I, O, E>
        extends
        AbstractMethodBinderBuilder<O, IUseCaseBinderBuilder<I, O, E>, IUseCaseBuilder<I, O, E>, IUseCase<I,O>>
        implements IUseCaseBinderBuilder<I, O, E> {

    public UseCaseBinderBuilder(IUseCaseBuilder<I, O, E> up, IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier) {
        super(up, supplier);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void doAutoDetection() throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doAutoDetection'");
    }

}

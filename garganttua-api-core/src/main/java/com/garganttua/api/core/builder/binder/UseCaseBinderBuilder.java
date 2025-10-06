package com.garganttua.api.core.builder.binder;

import com.garganttua.api.core.builder.BuilderException;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.IUseCaseBinderBuilder;
import com.garganttua.api.spec.engine.IUseCaseBuilder;

public class UseCaseBinderBuilder<Up>
        extends MethodBinderBuilder<IUseCaseBinderBuilder<IUseCaseBuilder<Up>>, IUseCaseBuilder<Up>>
        implements IUseCaseBinderBuilder<IUseCaseBuilder<Up>> {

    public UseCaseBinderBuilder(IUseCaseBuilder<Up> up, IObjectSupplierBuilder<?> supplier) throws BuilderException {
        super(up, supplier);
    }

    @Override
    protected IUseCaseBinderBuilder<IUseCaseBuilder<Up>> getReturned() {
        return this;
    }

}

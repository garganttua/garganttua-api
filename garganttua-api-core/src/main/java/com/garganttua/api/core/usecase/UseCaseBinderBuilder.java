package com.garganttua.api.core.usecase;
import com.garganttua.core.reflection.annotations.Reflected;

import com.garganttua.api.commons.context.IUseCase;
import com.garganttua.api.commons.context.dsl.IUseCaseBinderBuilder;
import com.garganttua.api.commons.context.dsl.IUseCaseBuilder;
import java.util.Set;

import com.garganttua.api.commons.ApiException;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

@Reflected
public class UseCaseBinderBuilder<I, O, E>
        extends
        AbstractMethodBinderBuilder<O, IUseCaseBinderBuilder<I, O, E>, IUseCaseBuilder<I, O, E>, IUseCase<I,O>>
        implements IUseCaseBinderBuilder<I, O, E> {

    public UseCaseBinderBuilder(IUseCaseBuilder<I, O, E> up, ISupplierBuilder<?, ? extends ISupplier<?>> supplier) {
        super(up, supplier, Set.of());
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // No auto-detection for use case binders - all configuration is explicit
    }

    @Override
    protected void doPreBuildWithDependency_(Object dependency) {
    }

    @Override
    protected void doPostBuildWithDependency(Object dependency) {
    }

    @Override
    protected void doAutoDetectionWithDependency(Object dependency) {
    }

}

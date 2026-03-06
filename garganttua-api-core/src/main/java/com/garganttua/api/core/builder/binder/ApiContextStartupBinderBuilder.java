package com.garganttua.api.core.builder.binder;

import java.util.Objects;
import java.util.Set;

import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.context.dsl.IApiContextStartupBinderBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public class ApiContextStartupBinderBuilder
        extends AbstractMethodBinderBuilder<Void, IApiContextStartupBinderBuilder, IApiContextBuilder, IMethodBinder<Void>>
        implements IApiContextStartupBinderBuilder {

    public ApiContextStartupBinderBuilder(IApiContextBuilder up,
            ISupplierBuilder<?, ? extends ISupplier<?>> supplier) {
        super(up, supplier, Set.of());
    }

    public ApiContextStartupBinderBuilder(IApiContextBuilder up,
            Object object) {
        super(up, FixedSupplierBuilder.of(Objects.requireNonNull(object, "Object cannot be null")), Set.of());
    }

    @Override
    protected void doAutoDetection() throws ApiException {
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

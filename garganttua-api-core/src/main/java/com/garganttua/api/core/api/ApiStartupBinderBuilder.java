package com.garganttua.api.core.api;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.Objects;
import java.util.Set;

import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.context.dsl.IApiStartupBinderBuilder;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

@Reflected
public class ApiStartupBinderBuilder
        extends AbstractMethodBinderBuilder<Void, IApiStartupBinderBuilder, IApiBuilder, IMethodBinder<Void>>
        implements IApiStartupBinderBuilder {

    public ApiStartupBinderBuilder(IApiBuilder up,
            ISupplierBuilder<?, ? extends ISupplier<?>> supplier) {
        super(up, supplier, Set.of());
    }

    public ApiStartupBinderBuilder(IApiBuilder up,
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

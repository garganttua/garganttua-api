package com.garganttua.api.core.builder.binder;

import java.util.Objects;

import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.context.dsl.IApiContextStartupBinderBuilder;
import com.garganttua.core.dsl.DslException;
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
        super(up, supplier);
    }

    public ApiContextStartupBinderBuilder(IApiContextBuilder up,
            Object object) {
        super(up, new FixedSupplierBuilder<>(Objects.requireNonNull(object, "Object cannot be null")));
    }

    @Override
    protected void doAutoDetection() throws DslException {
        // No auto-detection for startup binders - all configuration is explicit
    }

}

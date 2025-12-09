package com.garganttua.api.core.builder.binder;

import java.util.Objects;

import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.context.dsl.IApiContextStartupBinderBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.IObjectSupplier;
import com.garganttua.core.supply.dsl.FixedObjectSupplier;
import com.garganttua.core.supply.dsl.IObjectSupplierBuilder;

public class ApiContextStartupBinderBuilder
        extends AbstractMethodBinderBuilder<Void, IApiContextStartupBinderBuilder, IApiContextBuilder, IMethodBinder<Void>>
        implements IApiContextStartupBinderBuilder {

    public ApiContextStartupBinderBuilder(IApiContextBuilder up,
            IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier) {
        super(up, supplier);
    }

    public ApiContextStartupBinderBuilder(IApiContextBuilder up,
            Object object) {
        super(up, new FixedObjectSupplier<>(Objects.requireNonNull(object, "Object cannot be null")));
    }

    @Override
    protected void doAutoDetection() throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doAutoDetection'");
    }

}

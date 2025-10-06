package com.garganttua.api.core.builder.binder;

import java.util.Objects;

import com.garganttua.api.core.builder.BuilderException;
import com.garganttua.api.core.builder.supplier.FixedObjectSupplierBuilder;
import com.garganttua.api.spec.engine.IApplicationContextBuilder;
import com.garganttua.api.spec.engine.IApplicationContextStartupBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;

public class ApplicationContextStartupBinderBuilder
        extends MethodBinderBuilder<IApplicationContextStartupBinderBuilder, IApplicationContextBuilder>
        implements IApplicationContextStartupBinderBuilder {

    public ApplicationContextStartupBinderBuilder(IApplicationContextBuilder up,
            IObjectSupplierBuilder<?> supplier) throws BuilderException {
        super(up, supplier);
    }

    public ApplicationContextStartupBinderBuilder(IApplicationContextBuilder up,
            Object object) throws BuilderException {
        super(up, new FixedObjectSupplierBuilder<>(Objects.requireNonNull(object, "Object cannot be null")));
    }

    @Override
    protected IApplicationContextStartupBinderBuilder getReturned() {
        return this;
    }

}

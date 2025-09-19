package com.garganttua.api.core.engine;

import java.util.Objects;

import com.garganttua.api.spec.engine.IApplicationContextBuilder;
import com.garganttua.api.spec.engine.IApplicationContextStartupBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;

public class ApplicationContextStartupBinderBuilder
        extends MethodBinderBuilder<IApplicationContextStartupBinderBuilder, Object, IApplicationContextBuilder>
        implements IApplicationContextStartupBinderBuilder {

    protected ApplicationContextStartupBinderBuilder(IApplicationContextBuilder up,
            IObjectSupplierBuilder<?> supplier) throws BuilderException {
        super(up, supplier);
    }

    protected ApplicationContextStartupBinderBuilder(IApplicationContextBuilder up,
            Object object) throws BuilderException {
        super(up, new FixedObjectSupplierBuilder<>(Objects.requireNonNull(object, "Object cannot be null")));
    }

    @Override
    protected IApplicationContextStartupBinderBuilder getReturned() {
        return this;
    }

}

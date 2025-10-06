package com.garganttua.api.core.builder.binder;

import com.garganttua.api.core.builder.BuilderException;
import com.garganttua.api.spec.engine.IEntityBuilder;
import com.garganttua.api.spec.engine.IEntityMethodBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;

public class EntityMethodBinderBuilder extends MethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder>
        implements IEntityMethodBinderBuilder {

    public EntityMethodBinderBuilder(IEntityBuilder up, IObjectSupplierBuilder<?> supplier) throws BuilderException {
        super(up, supplier);
    }

    public EntityMethodBinderBuilder(IEntityBuilder up, IObjectSupplierBuilder<?> supplier, boolean collection) throws BuilderException {
        super(up, supplier, collection);
    }

    @Override
    protected IEntityMethodBinderBuilder getReturned() {
        return this;
    }

}

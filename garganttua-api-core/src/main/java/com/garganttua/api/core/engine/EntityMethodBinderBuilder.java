package com.garganttua.api.core.engine;

import com.garganttua.api.spec.engine.IEntityBuilder;
import com.garganttua.api.spec.engine.IEntityMethodBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;

public class EntityMethodBinderBuilder extends MethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder>
        implements IEntityMethodBinderBuilder {

    public EntityMethodBinderBuilder(IEntityBuilder up, IObjectSupplierBuilder<?> supplier) throws BuilderException {
        super(up, supplier);
    }

    @Override
    protected IEntityMethodBinderBuilder getReturned() {
        return this;
    }

}

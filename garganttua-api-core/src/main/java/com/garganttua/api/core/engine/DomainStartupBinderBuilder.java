package com.garganttua.api.core.engine;

import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IDomainStartupBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;

public class DomainStartupBinderBuilder extends MethodBinderBuilder<IDomainStartupBinderBuilder, Object, IDomainBuilder> implements IDomainStartupBinderBuilder {

    public DomainStartupBinderBuilder(IDomainBuilder up, IObjectSupplierBuilder<?> supplier) throws BuilderException {
        super(up, supplier);
    }

    @Override
    protected IDomainStartupBinderBuilder getReturned() {
        return this;
    }

}

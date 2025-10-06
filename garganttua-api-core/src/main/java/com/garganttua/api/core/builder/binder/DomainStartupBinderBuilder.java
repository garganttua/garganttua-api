package com.garganttua.api.core.builder.binder;

import com.garganttua.api.core.builder.BuilderException;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IDomainStartupBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;

public class DomainStartupBinderBuilder extends MethodBinderBuilder<IDomainStartupBinderBuilder, IDomainBuilder> implements IDomainStartupBinderBuilder {

    public DomainStartupBinderBuilder(IDomainBuilder up, IObjectSupplierBuilder<?> supplier) throws BuilderException {
        super(up, supplier);
    }

    @Override
    protected IDomainStartupBinderBuilder getReturned() {
        return this;
    }

}

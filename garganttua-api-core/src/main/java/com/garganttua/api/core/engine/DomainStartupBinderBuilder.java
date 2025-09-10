package com.garganttua.api.core.engine;

import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IDomainStartupBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplier;

public class DomainStartupBinderBuilder extends MethodBinderBuilder<IDomainStartupBinderBuilder, Object, IDomainBuilder> implements IDomainStartupBinderBuilder {

    public DomainStartupBinderBuilder(IDomainBuilder up, IObjectSupplier<?> supplier) {
        super(up, supplier);
    }

    @Override
    protected IDomainStartupBinderBuilder getReturned() {
        return this;
    }

}

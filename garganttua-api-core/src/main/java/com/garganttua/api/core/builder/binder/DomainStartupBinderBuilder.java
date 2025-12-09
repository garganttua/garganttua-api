package com.garganttua.api.core.builder.binder;

import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.IDomainStartupBinderBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.IObjectSupplier;
import com.garganttua.core.supply.dsl.IObjectSupplierBuilder;

public class DomainStartupBinderBuilder extends AbstractMethodBinderBuilder<Void, IDomainStartupBinderBuilder, IDomainBuilder, IMethodBinder<Void>> implements IDomainStartupBinderBuilder {

    public DomainStartupBinderBuilder(IDomainBuilder up, IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier) {
        super(up, supplier);
    }

    @Override
    protected void doAutoDetection() throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doAutoDetection'");
    }

}

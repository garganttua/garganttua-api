package com.garganttua.api.core.builder.binder;

import com.garganttua.api.spec.context.dsl.IEntityBuilder;
import com.garganttua.api.spec.context.dsl.IEntityMethodBinderBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.IObjectSupplier;
import com.garganttua.core.supply.dsl.IObjectSupplierBuilder;

public class EntityMethodBinderBuilder extends AbstractMethodBinderBuilder<Void, IEntityMethodBinderBuilder, IEntityBuilder, IMethodBinder<Void>>
        implements IEntityMethodBinderBuilder {

    public EntityMethodBinderBuilder(IEntityBuilder up, IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier) {
        super(up, supplier);
    }

    public EntityMethodBinderBuilder(IEntityBuilder up, IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier, boolean collection) {
        super(up, supplier, collection);
    }

    @Override
    protected void doAutoDetection() throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doAutoDetection'");
    }

}

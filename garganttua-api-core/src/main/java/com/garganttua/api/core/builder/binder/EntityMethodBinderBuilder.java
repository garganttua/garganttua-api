package com.garganttua.api.core.builder.binder;

import com.garganttua.api.spec.context.dsl.IEntityBuilder;
import com.garganttua.api.spec.context.dsl.IEntityMethodBinderBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public class EntityMethodBinderBuilder<E> extends AbstractMethodBinderBuilder<Void, IEntityMethodBinderBuilder<E>, IEntityBuilder<E>, IMethodBinder<Void>>
        implements IEntityMethodBinderBuilder<E> {

    public EntityMethodBinderBuilder(IEntityBuilder<E> up, ISupplierBuilder<?, ? extends ISupplier<?>> supplier) {
        super(up, supplier);
    }

    public EntityMethodBinderBuilder(IEntityBuilder<E> up, ISupplierBuilder<?, ? extends ISupplier<?>> supplier, boolean collection) {
        super(up, supplier, collection);
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // No auto-detection for entity method binders - all configuration is explicit
    }

}

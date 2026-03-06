package com.garganttua.api.core.builder.binder;

import java.util.Set;

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
        super(up, supplier, Set.of());
    }

    public EntityMethodBinderBuilder(IEntityBuilder<E> up, ISupplierBuilder<?, ? extends ISupplier<?>> supplier, boolean collection) {
        super(up, supplier, collection, Set.of());
    }

    @Override
    protected void doAutoDetection() throws ApiException {
    }

    @Override
    protected void doPreBuildWithDependency_(Object dependency) {
    }

    @Override
    protected void doPostBuildWithDependency(Object dependency) {
    }

    @Override
    protected void doAutoDetectionWithDependency(Object dependency) {
    }

}

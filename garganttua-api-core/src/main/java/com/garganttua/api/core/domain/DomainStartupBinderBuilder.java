package com.garganttua.api.core.domain;
import com.garganttua.core.reflection.annotations.Reflected;

import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.IDomainStartupBinderBuilder;
import java.util.Set;

import com.garganttua.api.commons.ApiException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

@Reflected
public class DomainStartupBinderBuilder<E> extends AbstractMethodBinderBuilder<Void, IDomainStartupBinderBuilder<E>, IDomainBuilder<E>, IMethodBinder<Void>> implements IDomainStartupBinderBuilder<E> {

    public DomainStartupBinderBuilder(IDomainBuilder<E> up, ISupplierBuilder<?, ? extends ISupplier<?>> supplier) {
        super(up, supplier, Set.of());
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // No auto-detection for domain startup binders - all configuration is explicit
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

package com.garganttua.api.core.builder.binder;

import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.IDomainStartupBinderBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public class DomainStartupBinderBuilder<E> extends AbstractMethodBinderBuilder<Void, IDomainStartupBinderBuilder<E>, IDomainBuilder<E>, IMethodBinder<Void>> implements IDomainStartupBinderBuilder<E> {

    public DomainStartupBinderBuilder(IDomainBuilder<E> up, ISupplierBuilder<?, ? extends ISupplier<?>> supplier) {
        super(up, supplier);
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // No auto-detection for domain startup binders - all configuration is explicit
    }

}

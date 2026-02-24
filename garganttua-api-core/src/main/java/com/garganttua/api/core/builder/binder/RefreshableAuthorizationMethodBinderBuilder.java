package com.garganttua.api.core.builder.binder;

import com.garganttua.api.spec.context.dsl.security.IRefreshableAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IRefreshableAuthorizationMethodBinderBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public class RefreshableAuthorizationMethodBinderBuilder<E> extends AbstractMethodBinderBuilder<E, IRefreshableAuthorizationMethodBinderBuilder<E>, IRefreshableAuthorizationBuilder<E>, IMethodBinder<E>> implements IRefreshableAuthorizationMethodBinderBuilder<E>{

    public RefreshableAuthorizationMethodBinderBuilder(IRefreshableAuthorizationBuilder<E> up,
            ISupplierBuilder<?, ? extends ISupplier<?>> supplier) {
        super(up, supplier);
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // No auto-detection for refreshable authorization method binders - all configuration is explicit
    }

}

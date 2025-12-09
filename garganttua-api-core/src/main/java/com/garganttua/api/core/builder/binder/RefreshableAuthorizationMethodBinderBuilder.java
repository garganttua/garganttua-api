package com.garganttua.api.core.builder.binder;

import com.garganttua.api.spec.context.dsl.security.IRefreshableAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IRefreshableAuthorizationMethodBinderBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.IObjectSupplier;
import com.garganttua.core.supply.dsl.IObjectSupplierBuilder;

public class RefreshableAuthorizationMethodBinderBuilder extends AbstractMethodBinderBuilder<Object, IRefreshableAuthorizationMethodBinderBuilder, IRefreshableAuthorizationBuilder, IMethodBinder<Object>> implements IRefreshableAuthorizationMethodBinderBuilder{

    public RefreshableAuthorizationMethodBinderBuilder(IRefreshableAuthorizationBuilder up,
            IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier) {
        super(up, supplier);
    }

    @Override
    protected void doAutoDetection() throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doAutoDetection'");
    }

}

package com.garganttua.api.core.builder.binder;

import com.garganttua.api.core.builder.BuilderException;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.IRefreshableAuthorizationBuilder;
import com.garganttua.api.spec.engine.IRefreshableAuthorizationMethodBinderBuilder;

public class RefreshableAuthorizationMethodBinderBuilder extends MethodBinderBuilder<IRefreshableAuthorizationMethodBinderBuilder, IRefreshableAuthorizationBuilder> implements IRefreshableAuthorizationMethodBinderBuilder{

    public RefreshableAuthorizationMethodBinderBuilder(IRefreshableAuthorizationBuilder up,
            IObjectSupplierBuilder<?> supplier) throws BuilderException {
        super(up, supplier);
    }

    @Override
    protected IRefreshableAuthorizationMethodBinderBuilder getReturned() {
        return this;
    }

}

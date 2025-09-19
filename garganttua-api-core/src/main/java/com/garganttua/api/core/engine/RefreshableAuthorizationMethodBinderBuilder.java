package com.garganttua.api.core.engine;

import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.IRefreshableAuthorizationBuilder;
import com.garganttua.api.spec.engine.IRefreshableAuthorizationMethodBinderBuilder;

public class RefreshableAuthorizationMethodBinderBuilder extends MethodBinderBuilder<IRefreshableAuthorizationMethodBinderBuilder, Object, IRefreshableAuthorizationBuilder> implements IRefreshableAuthorizationMethodBinderBuilder{

    protected RefreshableAuthorizationMethodBinderBuilder(IRefreshableAuthorizationBuilder up,
            IObjectSupplierBuilder<?> supplier) throws BuilderException {
        super(up, supplier);
    }

    @Override
    protected IRefreshableAuthorizationMethodBinderBuilder getReturned() {
        return this;
    }

}

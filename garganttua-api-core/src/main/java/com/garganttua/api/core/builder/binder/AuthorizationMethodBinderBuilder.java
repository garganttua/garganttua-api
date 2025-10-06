package com.garganttua.api.core.builder.binder;

import com.garganttua.api.core.builder.BuilderException;
import com.garganttua.api.spec.engine.IAuthorizationBuilder;
import com.garganttua.api.spec.engine.IAuthorizationMethodBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;

public class AuthorizationMethodBinderBuilder extends MethodBinderBuilder<IAuthorizationMethodBinderBuilder, IAuthorizationBuilder> implements IAuthorizationMethodBinderBuilder{

    public AuthorizationMethodBinderBuilder(IAuthorizationBuilder up, IObjectSupplierBuilder<?> supplier)
            throws BuilderException {
        super(up, supplier);
    }

    @Override
    protected IAuthorizationMethodBinderBuilder getReturned() {
        return this;
    }

}

package com.garganttua.api.core.builder;

import com.garganttua.api.core.engine.BuilderException;
import com.garganttua.api.core.engine.MethodBinderBuilder;
import com.garganttua.api.spec.engine.IAuthorizationBuilder;
import com.garganttua.api.spec.engine.IAuthorizationMethodBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;

public class AuthorizationMethodBinderBuilder extends MethodBinderBuilder<IAuthorizationMethodBinderBuilder, Object, IAuthorizationBuilder> implements IAuthorizationMethodBinderBuilder{

    public AuthorizationMethodBinderBuilder(IAuthorizationBuilder up, IObjectSupplierBuilder<?> supplier)
            throws BuilderException {
        super(up, supplier);
    }

    @Override
    protected IAuthorizationMethodBinderBuilder getReturned() {
        return this;
    }

}

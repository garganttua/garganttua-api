package com.garganttua.api.core.builder.binder;

import com.garganttua.api.core.builder.BuilderException;
import com.garganttua.api.spec.engine.IAuthenticationBuilder;
import com.garganttua.api.spec.engine.IAuthenticationMethodBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;

public class AuthenticationMethodBinderBuilder extends MethodBinderBuilder<IAuthenticationMethodBinderBuilder, IAuthenticationBuilder> implements IAuthenticationMethodBinderBuilder{

    public AuthenticationMethodBinderBuilder(IAuthenticationBuilder up, IObjectSupplierBuilder<?> supplier)
            throws BuilderException {
        super(up, supplier);
    }

    @Override
    protected IAuthenticationMethodBinderBuilder getReturned() {
        return this;
    }

}

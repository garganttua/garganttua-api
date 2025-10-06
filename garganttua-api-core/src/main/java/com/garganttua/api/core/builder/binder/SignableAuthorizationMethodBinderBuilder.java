package com.garganttua.api.core.builder.binder;

import com.garganttua.api.core.builder.BuilderException;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.ISignableAuthorizationBuilder;
import com.garganttua.api.spec.engine.ISignableAuthorizationMethodBinderBuilder;

public class SignableAuthorizationMethodBinderBuilder extends MethodBinderBuilder<ISignableAuthorizationMethodBinderBuilder, ISignableAuthorizationBuilder> implements ISignableAuthorizationMethodBinderBuilder{

    public SignableAuthorizationMethodBinderBuilder(ISignableAuthorizationBuilder up, IObjectSupplierBuilder<?> supplier)
            throws BuilderException {
        super(up, supplier);
    }

    @Override
    protected ISignableAuthorizationMethodBinderBuilder getReturned() {
        return this;
    }

}

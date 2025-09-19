package com.garganttua.api.core.engine;

import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.ISignableAuthorizationBuilder;
import com.garganttua.api.spec.engine.ISignableAuthorizationMethodBinderBuilder;

public class SignableAuthorizationMethodBinderBuilder extends MethodBinderBuilder<ISignableAuthorizationMethodBinderBuilder, Object, ISignableAuthorizationBuilder> implements ISignableAuthorizationMethodBinderBuilder{

    protected SignableAuthorizationMethodBinderBuilder(ISignableAuthorizationBuilder up, IObjectSupplierBuilder<?> supplier)
            throws BuilderException {
        super(up, supplier);
    }

    @Override
    protected ISignableAuthorizationMethodBinderBuilder getReturned() {
        return this;
    }

    

}

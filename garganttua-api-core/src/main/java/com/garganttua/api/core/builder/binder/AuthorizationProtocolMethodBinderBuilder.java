package com.garganttua.api.core.builder.binder;

import com.garganttua.api.core.builder.BuilderException;
import com.garganttua.api.spec.engine.IAuthorizationProtocolBuilder;
import com.garganttua.api.spec.engine.IAuthorizationProtocolMethodBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;

public class AuthorizationProtocolMethodBinderBuilder extends MethodBinderBuilder<IAuthorizationProtocolMethodBinderBuilder, IAuthorizationProtocolBuilder> implements IAuthorizationProtocolMethodBinderBuilder{

    public AuthorizationProtocolMethodBinderBuilder(IAuthorizationProtocolBuilder up,
            IObjectSupplierBuilder<?> supplier) throws BuilderException {
        super(up, supplier);
    }

    @Override
    protected AuthorizationProtocolMethodBinderBuilder getReturned() {
        return this;
    }

}

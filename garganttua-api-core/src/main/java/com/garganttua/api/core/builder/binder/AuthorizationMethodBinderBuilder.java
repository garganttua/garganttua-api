package com.garganttua.api.core.builder.binder;

import com.garganttua.api.spec.context.dsl.security.IAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationMethodBinderBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.IObjectSupplier;
import com.garganttua.core.supply.dsl.IObjectSupplierBuilder;

public class AuthorizationMethodBinderBuilder extends AbstractMethodBinderBuilder<Object, IAuthorizationMethodBinderBuilder, IAuthorizationBuilder, IMethodBinder<Object>> implements IAuthorizationMethodBinderBuilder{

    public AuthorizationMethodBinderBuilder(IAuthorizationBuilder up, IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier){
        super(up, supplier);
    }

    @Override
    protected void doAutoDetection() throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doAutoDetection'");
    }

}

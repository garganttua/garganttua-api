package com.garganttua.api.core.builder.binder;

import com.garganttua.api.spec.context.dsl.security.IAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationMethodBinderBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public class AuthorizationMethodBinderBuilder<E> extends AbstractMethodBinderBuilder<Object, IAuthorizationMethodBinderBuilder<E>, IAuthorizationBuilder<E>, IMethodBinder<Object>> implements IAuthorizationMethodBinderBuilder<E>{

    public AuthorizationMethodBinderBuilder(IAuthorizationBuilder<E> up, ISupplierBuilder<?, ? extends ISupplier<?>> supplier){
        super(up, supplier);
    }

    @Override
    protected void doAutoDetection() throws DslException {
        // No auto-detection for authorization method binders - all configuration is explicit
    }

}

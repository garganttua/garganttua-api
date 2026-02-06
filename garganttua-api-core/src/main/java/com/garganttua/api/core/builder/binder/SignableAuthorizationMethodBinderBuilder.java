package com.garganttua.api.core.builder.binder;

import com.garganttua.api.spec.context.dsl.security.ISignableAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.ISignableAuthorizationMethodBinderBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public class SignableAuthorizationMethodBinderBuilder<E> extends AbstractMethodBinderBuilder<E, ISignableAuthorizationMethodBinderBuilder<E>, ISignableAuthorizationBuilder<E>, IMethodBinder<E>> implements ISignableAuthorizationMethodBinderBuilder<E>{

    public SignableAuthorizationMethodBinderBuilder(ISignableAuthorizationBuilder<E> up, ISupplierBuilder<?, ? extends ISupplier<?>> supplier)
            {
        super(up, supplier);
    }

    @Override
    protected void doAutoDetection() throws DslException {
        // No auto-detection for signable authorization method binders - all configuration is explicit
    }

}

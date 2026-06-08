package com.garganttua.api.core.security.authorization;
import com.garganttua.core.reflection.annotations.Reflected;

import com.garganttua.api.commons.context.dsl.security.ISignableAuthorizationBuilder;
import com.garganttua.api.commons.context.dsl.security.ISignableAuthorizationMethodBinderBuilder;
import java.util.Set;

import com.garganttua.api.commons.ApiException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

@Reflected
public class SignableAuthorizationMethodBinderBuilder<E> extends AbstractMethodBinderBuilder<E, ISignableAuthorizationMethodBinderBuilder<E>, ISignableAuthorizationBuilder<E>, IMethodBinder<E>> implements ISignableAuthorizationMethodBinderBuilder<E>{

    public SignableAuthorizationMethodBinderBuilder(ISignableAuthorizationBuilder<E> up, ISupplierBuilder<?, ? extends ISupplier<?>> supplier)
            {
        super(up, supplier, Set.of());
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // No auto-detection for signable authorization method binders - all configuration is explicit
    }

    @Override
    protected void doPreBuildWithDependency_(Object dependency) {
    }

    @Override
    protected void doPostBuildWithDependency(Object dependency) {
    }

    @Override
    protected void doAutoDetectionWithDependency(Object dependency) {
    }

}

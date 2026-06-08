package com.garganttua.api.core.security.authenticator;
import com.garganttua.core.reflection.annotations.Reflected;

import com.garganttua.api.commons.context.dsl.security.IAuthenticatorBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorMethodBinderBuilder;
import java.util.Set;

import com.garganttua.api.commons.ApiException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

@Reflected
public class AuthenticatorMethodBinderBuilder<E> extends AbstractMethodBinderBuilder<Object, IAuthenticatorMethodBinderBuilder<E>, IAuthenticatorBuilder<E>, IMethodBinder<Object>> implements IAuthenticatorMethodBinderBuilder<E>{

    public AuthenticatorMethodBinderBuilder(IAuthenticatorBuilder<E> up, ISupplierBuilder<?, ? extends ISupplier<?>> supplier){
        super(up, supplier, Set.of());
    }

    public AuthenticatorMethodBinderBuilder(IAuthenticatorBuilder<E> up, ISupplierBuilder<?, ? extends ISupplier<?>> supplier, String methodName) {
        super(up, supplier, Set.of());
        this.method(methodName, null);
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // No auto-detection for authenticator method binders - all configuration is explicit
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

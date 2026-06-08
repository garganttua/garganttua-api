package com.garganttua.api.core.security.authorization;
import com.garganttua.core.reflection.annotations.Reflected;

import com.garganttua.api.commons.context.dsl.security.IAuthorizationBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthorizationMethodBinderBuilder;
import java.util.Set;

import com.garganttua.api.commons.ApiException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

@Reflected
public class AuthorizationMethodBinderBuilder<E> extends AbstractMethodBinderBuilder<Object, IAuthorizationMethodBinderBuilder<E>, IAuthorizationBuilder<E>, IMethodBinder<Object>> implements IAuthorizationMethodBinderBuilder<E>{

    public AuthorizationMethodBinderBuilder(IAuthorizationBuilder<E> up, ISupplierBuilder<?, ? extends ISupplier<?>> supplier){
        super(up, supplier, Set.of());
    }

    public AuthorizationMethodBinderBuilder(IAuthorizationBuilder<E> up, ISupplierBuilder<?, ? extends ISupplier<?>> supplier, String methodName) {
        super(up, supplier, Set.of());
        this.method(methodName, null);
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // No auto-detection for authorization method binders - all configuration is explicit
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

package com.garganttua.api.core.builder.binder;

import com.garganttua.api.spec.context.dsl.security.IAuthenticationBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticationMethodBinderBuilder;
import java.util.Set;

import com.garganttua.api.spec.ApiException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public class AuthenticationMethodBinderBuilder<ExecutionReturn> extends AbstractMethodBinderBuilder<ExecutionReturn, IAuthenticationMethodBinderBuilder<ExecutionReturn>, IAuthenticationBuilder, IMethodBinder<ExecutionReturn>> implements IAuthenticationMethodBinderBuilder<ExecutionReturn>{

    private String authenticateMethodName;

    public AuthenticationMethodBinderBuilder(IAuthenticationBuilder up, ISupplierBuilder<?, ? extends ISupplier<?>> supplier) {
        super(up, supplier, Set.of());
    }

    public AuthenticationMethodBinderBuilder(IAuthenticationBuilder up, ISupplierBuilder<?, ? extends ISupplier<?>> supplier, String methodName) {
        super(up, supplier, Set.of());
        this.authenticateMethodName = methodName;
    }

    public String getAuthenticateMethodName() {
        return this.authenticateMethodName;
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // No auto-detection for authentication method binders - all configuration is explicit
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

package com.garganttua.api.core.builder.binder;

import com.garganttua.api.spec.context.dsl.security.IAuthenticationBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticationMethodBinderBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public class AuthenticationMethodBinderBuilder<ExecutionReturn> extends AbstractMethodBinderBuilder<ExecutionReturn, IAuthenticationMethodBinderBuilder<ExecutionReturn>, IAuthenticationBuilder, IMethodBinder<ExecutionReturn>> implements IAuthenticationMethodBinderBuilder<ExecutionReturn>{

    public AuthenticationMethodBinderBuilder(IAuthenticationBuilder up, ISupplierBuilder<?, ? extends ISupplier<?>> supplier) {
        super(up, supplier);
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // No auto-detection for authentication method binders - all configuration is explicit
    }

}

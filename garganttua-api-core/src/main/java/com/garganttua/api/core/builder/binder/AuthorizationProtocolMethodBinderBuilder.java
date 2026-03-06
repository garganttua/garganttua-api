package com.garganttua.api.core.builder.binder;

import com.garganttua.api.spec.context.dsl.security.IAuthorizationProtocolBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationProtocolMethodBinderBuilder;
import java.util.Set;

import com.garganttua.api.spec.ApiException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public class AuthorizationProtocolMethodBinderBuilder extends AbstractMethodBinderBuilder<Object, IAuthorizationProtocolMethodBinderBuilder, IAuthorizationProtocolBuilder, IMethodBinder<Object>> implements IAuthorizationProtocolMethodBinderBuilder{

    public AuthorizationProtocolMethodBinderBuilder(IAuthorizationProtocolBuilder up,
            ISupplierBuilder<?, ? extends ISupplier<?>> supplier) {
        super(up, supplier, Set.of());
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // No auto-detection for authorization protocol method binders - all configuration is explicit
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

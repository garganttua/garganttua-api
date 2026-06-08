package com.garganttua.api.core.security.authorization;
import com.garganttua.core.reflection.annotations.Reflected;

import com.garganttua.api.commons.context.dsl.security.IRefreshableAuthorizationBuilder;
import com.garganttua.api.commons.context.dsl.security.IRefreshableAuthorizationMethodBinderBuilder;
import java.util.Set;

import com.garganttua.api.commons.ApiException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.AbstractMethodBinderBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

@Reflected
public class RefreshableAuthorizationMethodBinderBuilder<E> extends AbstractMethodBinderBuilder<E, IRefreshableAuthorizationMethodBinderBuilder<E>, IRefreshableAuthorizationBuilder<E>, IMethodBinder<E>> implements IRefreshableAuthorizationMethodBinderBuilder<E>{

    public RefreshableAuthorizationMethodBinderBuilder(IRefreshableAuthorizationBuilder<E> up,
            ISupplierBuilder<?, ? extends ISupplier<?>> supplier) {
        super(up, supplier, Set.of());
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // No auto-detection for refreshable authorization method binders - all configuration is explicit
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

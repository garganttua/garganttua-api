package com.garganttua.api.core.context.application;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.core.definition.AuthenticationDefinition;
import com.garganttua.api.spec.context.IAuthenticationContext;
import com.garganttua.api.spec.context.dsl.IUseCaseBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticationMethodBinderBuilder;
import com.garganttua.api.spec.definition.IAuthenticationDefinition;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public class AuthenticationContext implements IAuthenticationContext {

    private AuthenticationDefinition authenticationDefinition;

    public AuthenticationContext( Boolean findPrincipal,
            ISupplierBuilder<?, ? extends ISupplier<?>> supplier,
            IAuthenticationMethodBinderBuilder authenticateMethodBinder,
            List<Pair<Class<? extends Annotation>, Class<?>>> entityFieldAnnotations,
            IAuthenticationMethodBinderBuilder applySecurityOnEntityMethodBinder,
            Collection<IUseCaseBuilder<?, ?, ?>> useCasesMethodBinders ) {
        this.authenticationDefinition = new AuthenticationDefinition();
    }

    @Override
    public IAuthenticationDefinition getAuthenticationDefinition() {
        return this.authenticationDefinition;
    }

}

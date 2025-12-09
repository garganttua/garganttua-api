package com.garganttua.api.core.context.application;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.spec.context.IAuthenticationContext;
import com.garganttua.api.spec.context.dsl.IUseCaseBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticationBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticationMethodBinderBuilder;
import com.garganttua.core.supply.IObjectSupplierBuilder;

public class AuthenticationContext implements IAuthenticationContext {

    public AuthenticationContext( Boolean findPrincipal,
            IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier,
            IAuthenticationMethodBinderBuilder authenticateMethodBinder,
            List<Pair<Class<? extends Annotation>, Class<?>>> entityFieldAnnotations,
            IAuthenticationMethodBinderBuilder applySecurityOnEntityMethodBinder,
            Collection<IUseCaseBuilder<?, IAuthenticationBuilder>> useCasesMethodBinders ) {

    }

}

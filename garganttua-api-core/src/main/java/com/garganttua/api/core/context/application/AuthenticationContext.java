package com.garganttua.api.core.context.application;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.spec.engine.IAuthenticationBuilder;
import com.garganttua.api.spec.engine.IAuthenticationContext;
import com.garganttua.api.spec.engine.IAuthenticationMethodBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.IUseCaseBuilder;

public class AuthenticationContext implements IAuthenticationContext {

    public AuthenticationContext( Boolean findPrincipal,
            IObjectSupplierBuilder<?> supplier,
            IAuthenticationMethodBinderBuilder authenticateMethodBinder,
            List<Pair<Class<? extends Annotation>, Class<?>>> entityFieldAnnotations,
            IAuthenticationMethodBinderBuilder applySecurityOnEntityMethodBinder,
            Collection<IUseCaseBuilder<IAuthenticationBuilder>> useCasesMethodBinders ) {

    }

}

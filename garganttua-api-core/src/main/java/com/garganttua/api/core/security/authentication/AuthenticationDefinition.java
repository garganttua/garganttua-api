package com.garganttua.api.core.security.authentication;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.commons.context.dsl.IUseCaseBuilder;
import com.garganttua.api.commons.definition.IAuthenticationDefinition;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public record AuthenticationDefinition(
            ISupplierBuilder<?, ? extends ISupplier<?>> supplier,
            IMethodBinder<?> authenticateMethodBinder,
            List<Pair<IClass<? extends Annotation>, IClass<?>>> entityFieldAnnotations,
            IMethodBinder<?> applySecurityOnEntityMethodBinder,
            Collection<IUseCaseBuilder<?, ?, ?>> useCasesMethodBinders) implements IAuthenticationDefinition {

}

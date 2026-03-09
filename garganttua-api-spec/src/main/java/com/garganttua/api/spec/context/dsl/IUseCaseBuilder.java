package com.garganttua.api.spec.context.dsl;

import com.garganttua.api.spec.context.IUseCase;
import com.garganttua.api.spec.operation.Scope;
import com.garganttua.api.spec.operation.TechnicalOperation;
import com.garganttua.api.spec.context.dsl.security.IUseCaseSecurityBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public interface IUseCaseBuilder<I, O, E> extends IAutomaticLinkedBuilder<IUseCaseBuilder<I, O, E>, IDomainBuilder<E>, IUseCase<I, O>> {

    IUseCaseBuilder<I, O, E> pathSuffix(String string);

    IUseCaseBuilder<I, O, E> completePath(String string);

    IUseCaseBuilder<I, O, E> scope(Scope scope);

    IUseCaseBuilder<I, O, E> operation(TechnicalOperation operation);
    
    IUseCaseBinderBuilder<I, O, E> bind(ISupplierBuilder<?, ? extends ISupplier<?>> supplier) throws ApiException;

    IUseCaseBinderBuilder<I, O, E> bind(Object object) throws ApiException;

    IUseCaseBinderBuilder<I, O, E> bind();

    IUseCaseSecurityBuilder<I, O, E> security();

}

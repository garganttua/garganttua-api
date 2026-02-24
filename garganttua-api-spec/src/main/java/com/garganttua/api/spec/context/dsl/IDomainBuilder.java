package com.garganttua.api.spec.context.dsl;

import java.lang.reflect.Field;

import com.garganttua.api.spec.context.ContextBuildingStage;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.event.IEventPublisher;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public interface IDomainBuilder<E> extends IAutomaticLinkedBuilder<IDomainBuilder<E>, IApiContextBuilder, IDomainContext<E>> {

    IDomainStartupBinderBuilder<E> startup(ContextBuildingStage stage, ISupplierBuilder<?, ? extends ISupplier<?>> method) throws ApiException;

    IDomainBuilder<E> interfasse(ISupplierBuilder<? extends IInterface, ? extends ISupplier<? extends IInterface>> bean) throws ApiException;

    IDomainBuilder<E> interfasse(Class<? extends IInterface> interfasse) throws ApiException;

    IDomainBuilder<E> events(ISupplierBuilder<?, ? extends ISupplier<?>> bean) throws ApiException;

    IDomainBuilder<E> events(IEventPublisher eventPublisher) throws ApiException;

    IDomainBuilder<E> tenant(boolean b) throws ApiException;

    IDomainBuilder<E> owner(String string) throws ApiException;

    IDomainBuilder<E> owner(Field field) throws ApiException;

    IDomainBuilder<E> owner(ObjectAddress fieldAddress) throws ApiException;

    IDomainBuilder<E> owned(String string) throws ApiException;

    IDomainBuilder<E> owned(Field field) throws ApiException;

    IDomainBuilder<E> owned(ObjectAddress fieldAddress) throws ApiException;

    IDomainBuilder<E> publik();

    IDomainBuilder<E> shared(Field field) throws ApiException;

    IDomainBuilder<E> shared(String string) throws ApiException;

    IDomainBuilder<E> shared(ObjectAddress fieldAddress) throws ApiException;

    IDomainBuilder<E> hiddenable(String string) throws ApiException;

    IDomainBuilder<E> hiddenable(Field field) throws ApiException;

    IDomainBuilder<E> hiddenable(ObjectAddress fieldAddress) throws ApiException;

    IEntityBuilder<E> name(String name) throws ApiException;

    Class<E> getEntityClass() throws ApiException;

    IDomainSecurityBuilder<E> security() throws ApiException;

    <D> IDtoBuilder<E, D> dto(Class<D> dtoClass) throws ApiException;

    <I, O> IUseCaseBuilder<I, O, E> useCase(String useCaseName, Class<I> inputType, Class<O> outputType);

    IDomainWorkflowBuilder<E> workflow(String workflowName);

    IDomainBuilder<E> create(Object entity);

    IDomainBuilder<E> upsert(Object entity);

    IEntityBuilder<E> entity() throws ApiException;

}

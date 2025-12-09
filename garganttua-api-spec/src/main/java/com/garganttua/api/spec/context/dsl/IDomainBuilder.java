package com.garganttua.api.spec.context.dsl;

import java.lang.reflect.Field;

import com.garganttua.api.spec.context.ContextBuildingStage;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.domain.IDomainContext;
import com.garganttua.api.spec.event.IEventPublisher;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.supply.IObjectSupplier;
import com.garganttua.core.supply.dsl.IObjectSupplierBuilder;

public interface IDomainBuilder<E> extends IAutomaticLinkedBuilder<IDomainBuilder<E>, IApiContext, IDomainContext<E>> {

    IDomainStartupBinderBuilder<E> startup(ContextBuildingStage stage, IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> method) throws DslException;

    IDomainBuilder<E> interfasse(IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> bean) throws DslException;

    IDomainBuilder<E> interfasse(Class<? extends IInterface> interfasse) throws DslException;

    IDomainBuilder<E> creation(boolean b);

    IDomainBuilder<E> readAll(boolean b);

    IDomainBuilder<E> readOne(boolean b);

    IDomainBuilder<E> update(boolean b);

    IDomainBuilder<E> deleteAll(boolean b);

    IDomainBuilder<E> deleteOne(boolean b);

    IDomainBuilder<E> events(IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> bean) throws DslException;

    IDomainBuilder<E> events(IEventPublisher eventPublisher) throws DslException;

    IDomainBuilder<E> tenant(boolean b) throws DslException;

    IDomainBuilder<E> owner(String string) throws DslException;

    IDomainBuilder<E> owner(Field field) throws DslException;

    IDomainBuilder<E> owner(ObjectAddress fieldAddress) throws DslException;

    IDomainBuilder<E> owned(String string) throws DslException;

    IDomainBuilder<E> owned(Field field) throws DslException;

    IDomainBuilder<E> owned(ObjectAddress fieldAddress) throws DslException;

    IDomainBuilder<E> publik();

    IDomainBuilder<E> shared(Field field) throws DslException;

    IDomainBuilder<E> shared(String string) throws DslException;

    IDomainBuilder<E> shared(ObjectAddress fieldAddress) throws DslException;

    IDomainBuilder<E> hiddenable(String string) throws DslException;

    IDomainBuilder<E> hiddenable(Field field) throws DslException;

    IDomainBuilder<E> hiddenable(ObjectAddress fieldAddress) throws DslException;

    IEntityBuilder<E> name(String name) throws DslException;

    Class<E> getEntityClass() throws DslException;

    IDomainSecurityBuilder<E> security() throws DslException;

    <D> IDtoBuilder<E, D> dto(Class<D> dtoClass) throws DslException;

    <I, O> IUseCaseBuilder<I, O, E> useCase(String useCaseName, Class<I> inputType, Class<O> outputType);

    IDomainBuilder<E> create(Object entity);

    IDomainBuilder<E> upsert(Object entity);

    IEntityBuilder<E> entity() throws DslException;

}

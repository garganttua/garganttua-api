package com.garganttua.api.spec.engine;

import java.lang.reflect.Field;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.event.IEventPublisher;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.api.spec.security.IDomainSecurityBuilder;
import com.garganttua.reflection.GGObjectAddress;

public interface IDomainBuilder extends IAutomaticLinkedBuilder<Object, IApplicationContextBuilder, IDomainBuilder> {

    IMethodBinderBuilder<IDomainStartupBinderBuilder, Object, IDomainBuilder>  startup(ContextBuildingStage stage, IObjectSupplierBuilder<?> method) throws CoreException;

    IDomainBuilder interfasse(IObjectSupplierBuilder<?> bean) throws CoreException;

    IDomainBuilder interfasse(IInterface interfasse) throws CoreException;

    IDomainBuilder creation(boolean b);

    IDomainBuilder readAll(boolean b);

    IDomainBuilder readOne(boolean b);

    IDomainBuilder update(boolean b);

    IDomainBuilder deleteAll(boolean b);

    IDomainBuilder deleteOne(boolean b);

    IDomainBuilder events(IObjectSupplierBuilder<?> bean) throws CoreException;

    IDomainBuilder events(IEventPublisher eventPublisher) throws CoreException;

    IDomainBuilder tenant(boolean b) throws CoreException;

    IDomainBuilder owner(String string) throws CoreException;

    IDomainBuilder owner(Field field) throws CoreException;

    IDomainBuilder owner(GGObjectAddress fieldAddress) throws CoreException;

    IDomainBuilder owned(String string) throws CoreException;

    IDomainBuilder owned(Field field) throws CoreException;

    IDomainBuilder owned(GGObjectAddress fieldAddress) throws CoreException;

    IDomainBuilder publik();

    IDomainBuilder shared(Field field) throws CoreException;

    IDomainBuilder shared(String string) throws CoreException;

    IDomainBuilder shared(GGObjectAddress fieldAddress) throws CoreException;

    IDomainBuilder hiddenable(String string) throws CoreException;

    IDomainBuilder hiddenable(Field field) throws CoreException;

    IDomainBuilder hiddenable(GGObjectAddress fieldAddress) throws CoreException;

    IAuthorizationBuilder authorization() throws CoreException;

    IAuthenticatorBuilder authenticator() throws CoreException;

    IEntityBuilder entity(Class<?> entityClass) throws CoreException;

    Class<?> getEntityClass() throws CoreException;

    IDomainSecurityBuilder security();

    IDtoBuilder dto(Class<?> dtoClass);

    IUseCaseBuilder useCase(String useCaseName);

    IDomainBuilder create(Object entity);

    IDomainBuilder upsert(Object entity);

}

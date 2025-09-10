package com.garganttua.api.spec.engine;

import java.lang.reflect.Field;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.event.IEventPublisher;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.api.spec.security.ISecurityBuilder;
import com.garganttua.reflection.GGObjectAddress;

public interface IDomainBuilder {

    IMethodBinderBuilder<IDomainStartupBinderBuilder, Object, IDomainBuilder>  startup(ContextBuildingStage stage, IObjectSupplier<?> method);

    IDomainBuilder interfasse(IObjectSupplier<?> bean) throws CoreException;

    IDomainBuilder interfasse(IInterface interfasse) throws CoreException;

    IDomainBuilder creation(boolean b);

    IDomainBuilder readAll(boolean b);

    IDomainBuilder readOne(boolean b);

    IDomainBuilder update(boolean b);

    IDomainBuilder deleteAll(boolean b);

    IDomainBuilder deleteOne(boolean b);

    IDomainBuilder events(IObjectSupplier<?> bean) throws CoreException;

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

    IAuthorizationBuilder authorization();

    IAuthenticatorBuilder authenticator();

    IEntityBuilder entity(Class<?> class1) throws CoreException;

    ISecurityBuilder security();

    IDomainBuilder autoDetectDtos(boolean b);

    IDtoBuilder dto(Class<?> class1);

    IUseCaseBuilder useCase(String string);

    IContext build();

    IDomainBuilder autoDetectUseCases(boolean b);

    IDomainBuilder create(Object entity);

    IDomainBuilder upsert(Object entity);

}

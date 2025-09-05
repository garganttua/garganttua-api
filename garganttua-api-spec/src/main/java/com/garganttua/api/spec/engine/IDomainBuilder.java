package com.garganttua.api.spec.engine;

import java.lang.reflect.Field;

import com.garganttua.api.spec.security.ISecurityBuilder;
import com.garganttua.reflection.GGObjectAddress;

public interface IDomainBuilder {

    IDomainStartupBinderBuilder startup(IObjectSupplier<?> method);

    IDomainBuilder interfasse(IObjectSupplier<?> bean);

    IDomainBuilder creation(boolean b);

    IDomainBuilder readAll(boolean b);

    IDomainBuilder readOne(boolean b);

    IDomainBuilder update(boolean b);

    IDomainBuilder deleteAll(boolean b);

    IDomainBuilder deleteOne(boolean b);

    IDomainBuilder events(IObjectSupplier<?> bean);

    IDomainBuilder tenant(String field);

    IDomainBuilder tenant(Field field);

    IDomainBuilder tenant(GGObjectAddress fieldAddress);

    IDomainBuilder owner(String string);

    IDomainBuilder owner(Field field);

    IDomainBuilder owner(GGObjectAddress fieldAddress);

    IDomainBuilder owned(String string);

    IDomainBuilder owned(Field field);

    IDomainBuilder owned(GGObjectAddress fieldAddress);

    IDomainBuilder publik();

    IDomainBuilder shared(Field field);

    IDomainBuilder shared(String string);

    IDomainBuilder shared(GGObjectAddress fieldAddress);

    IDomainBuilder hiddenable(String string);

    IDomainBuilder hiddenable(Field field);

    IDomainBuilder hiddenable(GGObjectAddress fieldAddress);

    IAuthorizationBuilder authorization();

    IAuthenticatorBuilder authenticator();

    IEntityBuilder entity(Class<Object> class1);

    ISecurityBuilder security();

    IDomainBuilder autoDetectDtos(boolean b);

    IDtoBuilder dto(Class<Object> class1);

    IUseCaseBuilder useCase(String string);

    IContext build();

    IDomainBuilder tenant(boolean b);

    IDomainBuilder autoDetectUseCases(boolean b);

}

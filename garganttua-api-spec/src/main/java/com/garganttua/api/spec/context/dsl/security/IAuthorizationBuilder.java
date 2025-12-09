package com.garganttua.api.spec.context.dsl.security;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.garganttua.api.spec.context.IAuthorizationContext;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;

public interface IAuthorizationBuilder<E>
        extends IAutomaticLinkedBuilder<IAuthorizationBuilder<E>, IDomainSecurityBuilder<E>, IAuthorizationContext> {

    IAuthorizationBuilder<E> type(Field field) throws DslException;

    IAuthorizationBuilder<E> type(String fieldName) throws DslException;

    IAuthorizationBuilder<E> type(ObjectAddress fieldAddress) throws DslException;

    IAuthorizationBuilder<E> authorities(String fieldName) throws DslException;

    IAuthorizationBuilder<E> authorities(Field field) throws DslException;

    IAuthorizationBuilder<E> authorities(ObjectAddress fieldAddress) throws DslException;

    IAuthorizationBuilder<E> expirable(ObjectAddress fieldAddress) throws DslException;

    IAuthorizationBuilder<E> expirable(Field field) throws DslException;

    IAuthorizationBuilder<E> expirable(String fieldName) throws DslException;

    IAuthorizationBuilder<E> revokable(String fieldName) throws DslException;

    IAuthorizationBuilder<E> revokable(Field field) throws DslException;

    IAuthorizationBuilder<E> revokable(ObjectAddress fieldAddress) throws DslException;

    IAuthorizationMethodBinderBuilder<E> encode(Method method) throws DslException;

    IAuthorizationMethodBinderBuilder<E> encode(String methodName) throws DslException;

    IAuthorizationMethodBinderBuilder<E> encode(ObjectAddress methodAddress) throws DslException;

    IAuthorizationMethodBinderBuilder<E> decode(Method method) throws DslException;

    IAuthorizationMethodBinderBuilder<E> decode(String methodName) throws DslException;

    IAuthorizationMethodBinderBuilder<E> decode(ObjectAddress methodAddress) throws DslException;

    IAuthorizationBuilder<E> storable(boolean b);

    IRefreshableAuthorizationBuilder<E> refreshable();

    ISignableAuthorizationBuilder<E> signable();

    Boolean isStorable();

    Boolean isRefreshable();

}

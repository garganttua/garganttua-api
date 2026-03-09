package com.garganttua.api.spec.context.dsl.security;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.garganttua.api.spec.security.context.IAuthorizationContext;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;

public interface IAuthorizationBuilder<E>
        extends IAutomaticLinkedBuilder<IAuthorizationBuilder<E>, IDomainSecurityBuilder<E>, IAuthorizationContext> {

    IAuthorizationBuilder<E> type(Field field) throws ApiException;

    IAuthorizationBuilder<E> type(String fieldName) throws ApiException;

    IAuthorizationBuilder<E> type(ObjectAddress fieldAddress) throws ApiException;

    IAuthorizationBuilder<E> authorities(String fieldName) throws ApiException;

    IAuthorizationBuilder<E> authorities(Field field) throws ApiException;

    IAuthorizationBuilder<E> authorities(ObjectAddress fieldAddress) throws ApiException;

    IAuthorizationBuilder<E> expirable(ObjectAddress fieldAddress) throws ApiException;

    IAuthorizationBuilder<E> expirable(Field field) throws ApiException;

    IAuthorizationBuilder<E> expirable(String fieldName) throws ApiException;

    IAuthorizationBuilder<E> revokable(String fieldName) throws ApiException;

    IAuthorizationBuilder<E> revokable(Field field) throws ApiException;

    IAuthorizationBuilder<E> revokable(ObjectAddress fieldAddress) throws ApiException;

    IAuthorizationMethodBinderBuilder<E> encode(Method method) throws ApiException;

    IAuthorizationMethodBinderBuilder<E> encode(String methodName) throws ApiException;

    IAuthorizationMethodBinderBuilder<E> encode(ObjectAddress methodAddress) throws ApiException;

    IAuthorizationMethodBinderBuilder<E> decode(Method method) throws ApiException;

    IAuthorizationMethodBinderBuilder<E> decode(String methodName) throws ApiException;

    IAuthorizationMethodBinderBuilder<E> decode(ObjectAddress methodAddress) throws ApiException;

    IAuthorizationBuilder<E> storable(boolean b);

    IRefreshableAuthorizationBuilder<E> refreshable();

    ISignableAuthorizationBuilder<E> signable();

    Boolean isStorable();

    Boolean isRefreshable();

}

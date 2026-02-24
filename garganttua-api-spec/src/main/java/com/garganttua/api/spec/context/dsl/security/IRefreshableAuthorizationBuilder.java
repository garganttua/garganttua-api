package com.garganttua.api.spec.context.dsl.security;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.garganttua.api.spec.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;

public interface IRefreshableAuthorizationBuilder<E>
        extends IAutomaticLinkedBuilder<IRefreshableAuthorizationBuilder<E>, IAuthorizationBuilder<E>, E> {

    IRefreshableAuthorizationBuilder<E> expirable(ObjectAddress fieldAddress) throws ApiException;

    IRefreshableAuthorizationBuilder<E> expirable(Field field) throws ApiException;

    IRefreshableAuthorizationBuilder<E> expirable(String fieldName) throws ApiException;

    IRefreshableAuthorizationBuilder<E> revokable(String fieldName) throws ApiException;

    IRefreshableAuthorizationBuilder<E> revokable(Field field) throws ApiException;

    IRefreshableAuthorizationBuilder<E> revokable(ObjectAddress fieldAddress) throws ApiException;

    IRefreshableAuthorizationBuilder<E> encode(Method method) throws ApiException;

    IRefreshableAuthorizationBuilder<E> encode(String methodName) throws ApiException;

    IRefreshableAuthorizationBuilder<E> encode(ObjectAddress fieldAddress) throws ApiException;

    IRefreshableAuthorizationBuilder<E> decode(Method method) throws ApiException;

    IRefreshableAuthorizationBuilder<E> decode(String methodName) throws ApiException;

    IRefreshableAuthorizationBuilder<E> decode(ObjectAddress fieldAddress) throws ApiException;

    /*
     * IRefreshableAuthorizationBuilder validateAgainst(String methodName) throws
     * CoreException;
     * 
     * IRefreshableAuthorizationBuilder validateAgainst(Method method) throws
     * CoreException;
     * 
     * IRefreshableAuthorizationBuilder validateAgainst(ObjectAddress
     * fieldAddress) throws ApiException;
     */

}

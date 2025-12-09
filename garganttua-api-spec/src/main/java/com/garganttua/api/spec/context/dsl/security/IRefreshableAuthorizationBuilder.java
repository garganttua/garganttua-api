package com.garganttua.api.spec.context.dsl.security;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.garganttua.core.dsl.DslException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;

public interface IRefreshableAuthorizationBuilder<E>
        extends IAutomaticLinkedBuilder<IRefreshableAuthorizationBuilder<E>, IAuthorizationBuilder<E>, E> {

    IRefreshableAuthorizationBuilder<E> expirable(ObjectAddress fieldAddress) throws DslException;

    IRefreshableAuthorizationBuilder<E> expirable(Field field) throws DslException;

    IRefreshableAuthorizationBuilder<E> expirable(String fieldName) throws DslException;

    IRefreshableAuthorizationBuilder<E> revokable(String fieldName) throws DslException;

    IRefreshableAuthorizationBuilder<E> revokable(Field field) throws DslException;

    IRefreshableAuthorizationBuilder<E> revokable(ObjectAddress fieldAddress) throws DslException;

    IRefreshableAuthorizationBuilder<E> encode(Method method) throws DslException;

    IRefreshableAuthorizationBuilder<E> encode(String methodName) throws DslException;

    IRefreshableAuthorizationBuilder<E> encode(ObjectAddress fieldAddress) throws DslException;

    IRefreshableAuthorizationBuilder<E> decode(Method method) throws DslException;

    IRefreshableAuthorizationBuilder<E> decode(String methodName) throws DslException;

    IRefreshableAuthorizationBuilder<E> decode(ObjectAddress fieldAddress) throws DslException;

    /*
     * IRefreshableAuthorizationBuilder validateAgainst(String methodName) throws
     * CoreException;
     * 
     * IRefreshableAuthorizationBuilder validateAgainst(Method method) throws
     * CoreException;
     * 
     * IRefreshableAuthorizationBuilder validateAgainst(ObjectAddress
     * fieldAddress) throws CoreException;
     */

}

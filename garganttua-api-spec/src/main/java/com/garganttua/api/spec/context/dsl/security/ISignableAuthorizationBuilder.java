package com.garganttua.api.spec.context.dsl.security;

import java.lang.reflect.Method;

import com.garganttua.core.dsl.DslException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;

public interface ISignableAuthorizationBuilder<E> extends IAutomaticLinkedBuilder<ISignableAuthorizationBuilder<E>, IAuthorizationBuilder<E>, Object> {

    ISignableAuthorizationBuilder<E> setSignature(String string) throws DslException;

    ISignableAuthorizationBuilder<E> setSignature(Method method) throws DslException;

    ISignableAuthorizationBuilder<E> setSignature(ObjectAddress fieldAddress) throws DslException;

    ISignableAuthorizationBuilder<E> getSignature(String string) throws DslException;

    ISignableAuthorizationBuilder<E> getSignature(Method method) throws DslException;

    ISignableAuthorizationBuilder<E> getSignature(ObjectAddress fieldAddress) throws DslException;

    ISignableAuthorizationBuilder<E> getDataToSign(String string) throws DslException;

    ISignableAuthorizationBuilder<E> getDataToSign(Method method) throws DslException;

    ISignableAuthorizationBuilder<E> getDataToSign(ObjectAddress fieldAddress) throws DslException;

}

package com.garganttua.api.spec.context.dsl.security;

import java.lang.reflect.Method;

import com.garganttua.api.spec.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;

public interface ISignableAuthorizationBuilder<E> extends IAutomaticLinkedBuilder<ISignableAuthorizationBuilder<E>, IAuthorizationBuilder<E>, Object> {

    ISignableAuthorizationBuilder<E> setSignature(String string) throws ApiException;

    ISignableAuthorizationBuilder<E> setSignature(Method method) throws ApiException;

    ISignableAuthorizationBuilder<E> setSignature(ObjectAddress fieldAddress) throws ApiException;

    ISignableAuthorizationBuilder<E> getSignature(String string) throws ApiException;

    ISignableAuthorizationBuilder<E> getSignature(Method method) throws ApiException;

    ISignableAuthorizationBuilder<E> getSignature(ObjectAddress fieldAddress) throws ApiException;

    ISignableAuthorizationBuilder<E> getDataToSign(String string) throws ApiException;

    ISignableAuthorizationBuilder<E> getDataToSign(Method method) throws ApiException;

    ISignableAuthorizationBuilder<E> getDataToSign(ObjectAddress fieldAddress) throws ApiException;

}

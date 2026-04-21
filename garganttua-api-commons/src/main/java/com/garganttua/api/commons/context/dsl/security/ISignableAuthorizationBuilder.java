package com.garganttua.api.commons.context.dsl.security;

import com.garganttua.api.commons.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.IMethod;
import com.garganttua.core.reflection.ObjectAddress;

public interface ISignableAuthorizationBuilder<E> extends IAutomaticLinkedBuilder<ISignableAuthorizationBuilder<E>, IAuthorizationBuilder<E>, Object> {

    ISignableAuthorizationBuilder<E> signature(String string) throws ApiException;

    ISignableAuthorizationBuilder<E> signature(IField method) throws ApiException;

    ISignableAuthorizationBuilder<E> signature(ObjectAddress fieldAddress) throws ApiException;

    ISignableAuthorizationBuilder<E> getDataToSign(String string) throws ApiException;

    ISignableAuthorizationBuilder<E> getDataToSign(IMethod method) throws ApiException;

    ISignableAuthorizationBuilder<E> getDataToSign(ObjectAddress fieldAddress) throws ApiException;

}

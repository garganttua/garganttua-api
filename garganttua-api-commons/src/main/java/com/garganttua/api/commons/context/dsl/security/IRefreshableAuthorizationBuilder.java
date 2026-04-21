package com.garganttua.api.commons.context.dsl.security;

import com.garganttua.api.commons.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.IMethod;
import com.garganttua.core.reflection.ObjectAddress;

public interface IRefreshableAuthorizationBuilder<E>
        extends IAutomaticLinkedBuilder<IRefreshableAuthorizationBuilder<E>, IAuthorizationBuilder<E>, E> {

    IRefreshableAuthorizationBuilder<E> expirable(ObjectAddress fieldAddress) throws ApiException;

    IRefreshableAuthorizationBuilder<E> expirable(IField field) throws ApiException;

    IRefreshableAuthorizationBuilder<E> expirable(String fieldName) throws ApiException;

    IRefreshableAuthorizationBuilder<E> revokable(String fieldName) throws ApiException;

    IRefreshableAuthorizationBuilder<E> revokable(IField field) throws ApiException;

    IRefreshableAuthorizationBuilder<E> revokable(ObjectAddress fieldAddress) throws ApiException;

    IRefreshableAuthorizationBuilder<E> encode(IMethod method) throws ApiException;

    IRefreshableAuthorizationBuilder<E> encode(String methodName) throws ApiException;

    IRefreshableAuthorizationBuilder<E> encode(ObjectAddress fieldAddress) throws ApiException;

    IRefreshableAuthorizationBuilder<E> decode(IMethod method) throws ApiException;

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

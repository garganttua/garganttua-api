package com.garganttua.api.commons.context.dsl.security;

import com.garganttua.core.reflection.IField;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.security.context.IAuthorizationContext;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;

public interface IAuthorizationBuilder<E>
        extends IAutomaticLinkedBuilder<IAuthorizationBuilder<E>, IDomainSecurityBuilder<E>, IAuthorizationContext> {

    IAuthorizationBuilder<E> type(IField field) throws ApiException;

    IAuthorizationBuilder<E> type(String fieldName) throws ApiException;

    IAuthorizationBuilder<E> type(ObjectAddress fieldAddress) throws ApiException;

    IAuthorizationBuilder<E> authorities(String fieldName) throws ApiException;

    IAuthorizationBuilder<E> authorities(IField field) throws ApiException;

    IAuthorizationBuilder<E> authorities(ObjectAddress fieldAddress) throws ApiException;

    IAuthorizationBuilder<E> expirable(ObjectAddress fieldAddress) throws ApiException;

    IAuthorizationBuilder<E> expirable(IField field) throws ApiException;

    IAuthorizationBuilder<E> expirable(String fieldName) throws ApiException;

    IAuthorizationBuilder<E> creation(ObjectAddress fieldAddress) throws ApiException;

    IAuthorizationBuilder<E> creation(IField field) throws ApiException;

    IAuthorizationBuilder<E> creation(String fieldName) throws ApiException;

    IAuthorizationBuilder<E> signedBy(ObjectAddress fieldAddress) throws ApiException;

    IAuthorizationBuilder<E> signedBy(IField field) throws ApiException;

    IAuthorizationBuilder<E> signedBy(String fieldName) throws ApiException;

    IAuthorizationBuilder<E> revokable(String fieldName) throws ApiException;

    IAuthorizationBuilder<E> revokable(IField field) throws ApiException;

    IAuthorizationBuilder<E> revokable(ObjectAddress fieldAddress) throws ApiException;

   /*  IAuthorizationMethodBinderBuilder<E> encode(IMethod method) throws ApiException;

    IAuthorizationMethodBinderBuilder<E> encode(String methodName) throws ApiException;

    IAuthorizationMethodBinderBuilder<E> encode(ObjectAddress methodAddress) throws ApiException;

    IAuthorizationMethodBinderBuilder<E> decode(IMethod method) throws ApiException;

    IAuthorizationMethodBinderBuilder<E> decode(String methodName) throws ApiException;

    IAuthorizationMethodBinderBuilder<E> decode(ObjectAddress methodAddress) throws ApiException; */

    IAuthorizationBuilder<E> storable(boolean b);

    // Custom token production (the mint-side issuer) moved to the authentication
    // builder: .authentication(supplier).authorization(issuer, "method"). See
    // IAuthenticationBuilder.authorization(...).

    IRefreshableAuthorizationBuilder<E> refreshable();

    ISignableAuthorizationBuilder<E> signable();

    Boolean isStorable();

    Boolean isRefreshable();

}

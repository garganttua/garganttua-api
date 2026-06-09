package com.garganttua.api.commons.context.dsl.security;

import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.IMethod;

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

    /**
     * Declares the method that encodes this authorization to its transport form
     * (e.g. a JWT {@code header.payload.signature}). Available on the plain
     * authorization — a token need NOT be refreshable to be encoded. The encoded
     * form becomes the output of {@code authenticate} / {@code refreshAuthorization}.
     */
    IAuthorizationBuilder<E> encode(IMethod method) throws ApiException;

    IAuthorizationBuilder<E> encode(String methodName) throws ApiException;

    IAuthorizationBuilder<E> encode(ObjectAddress methodAddress) throws ApiException;

    /**
     * Declares the method that decodes this authorization from its transport form.
     * Symmetric to {@link #encode(String)}; available on the plain authorization.
     */
    IAuthorizationBuilder<E> decode(IMethod method) throws ApiException;

    IAuthorizationBuilder<E> decode(String methodName) throws ApiException;

    IAuthorizationBuilder<E> decode(ObjectAddress methodAddress) throws ApiException;

    IAuthorizationBuilder<E> storable(boolean b);

    // Custom token production (the mint-side issuer) moved to the authentication
    // builder: .authentication(supplier).authorization(issuer, "method"). See
    // IAuthenticationBuilder.authorization(...).

    IRefreshableAuthorizationBuilder<E> refreshable();

    ISignableAuthorizationBuilder<E> signable();

    Boolean isStorable();

    Boolean isRefreshable();

}

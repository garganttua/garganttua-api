package com.garganttua.api.commons.context.dsl.security;

import com.garganttua.core.reflection.IField;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.security.context.IAuthorizationContext;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

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

    /**
     * Declares a custom token-production method — the mint-side dual of the
     * verify-side {@code .authentication(supplier).authenticate("method")}. The
     * {@code supplier} provides the instance holding {@code methodName}; the
     * returned builder binds the method's parameters via {@code .withParam(i,
     * supplier)} (free signature, resolved from the runtime context exactly like
     * the authenticate side). The bound method returns the produced authorization
     * entity. When set, the framework delegates token production (shape +
     * signature) to it instead of its built-in minting; persistence and
     * transport encoding still run around it. When left unset, the standard
     * framework minting runs. Enables custom tokens and delegation to an external
     * authorization server (Keycloak / OAuth2).
     */
    IAuthorizationMethodBinderBuilder<E> issuer(
            ISupplierBuilder<?, ? extends ISupplier<?>> supplier, String methodName) throws ApiException;

    IRefreshableAuthorizationBuilder<E> refreshable();

    ISignableAuthorizationBuilder<E> signable();

    Boolean isStorable();

    Boolean isRefreshable();

}

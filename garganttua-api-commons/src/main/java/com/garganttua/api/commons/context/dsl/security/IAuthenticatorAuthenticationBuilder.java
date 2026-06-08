package com.garganttua.api.commons.context.dsl.security;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.security.context.IAuthenticatorAuthenticationContext;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

/**
 * Per-authentication sub-builder returned by
 * {@link IAuthenticatorBuilder#authentication(IAuthenticationBuilder)}. Home of
 * the authenticator's authorization: the token domain {@link #authorization(IDomainBuilder)}
 * and the custom token-production (mint) method {@link #authorization(ISupplierBuilder, String)},
 * the mint-side dual of the verify-side {@code .authenticate(...)}. {@code .up()}
 * returns the authenticator builder.
 *
 * @param <E> the authenticator entity type
 */
public interface IAuthenticatorAuthenticationBuilder<E> extends
        IAutomaticLinkedBuilder<IAuthenticatorAuthenticationBuilder<E>, IAuthenticatorBuilder<E>, IAuthenticatorAuthenticationContext> {

    /**
     * Declares the persisted token (authorization) domain backing this
     * authenticator and returns its sub-builder ({@code .lifeTime()},
     * {@code .refreshLifeTime()}, {@code .key(...)}). {@code .up()} on it returns
     * the authenticator builder.
     */
    IAuthenticatorAuthorizationBuilder<E> authorization(IDomainBuilder<E> authorizationDomain) throws ApiException;

    /**
     * Declares a custom token-production (mint) method — the mint-side dual of
     * the verify-side {@code authenticate}. The {@code supplier} provides the
     * instance holding {@code methodName} (it may be a different object than the
     * authenticator entity — e.g. a Keycloak/OAuth2 issuer); the returned builder
     * binds the method's parameters via {@code .withParam(i, supplier)}, resolved
     * from the runtime context exactly like the authenticate side. The bound
     * method returns the produced authorization entity. When set, the framework
     * delegates token production (shape + signature) to it instead of its
     * built-in minting; persistence and transport encoding still run around it.
     * When left unset, the standard framework minting runs. {@code .up()} on the
     * returned binder returns the authenticator builder.
     */
    IAuthenticatorMethodBinderBuilder<E> authorization(
            ISupplierBuilder<?, ? extends ISupplier<?>> supplier, String methodName) throws ApiException;


}

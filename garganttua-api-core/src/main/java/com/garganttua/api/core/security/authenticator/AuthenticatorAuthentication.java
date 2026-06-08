package com.garganttua.api.core.security.authenticator;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.Objects;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorAuthenticationBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorAuthorizationBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorMethodBinderBuilder;
import com.garganttua.api.commons.security.context.IAuthenticatorAuthenticationContext;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

/**
 * Per-authentication sub-builder of {@link AuthenticatorBuilder}. A thin
 * navigation node ({@code .up()} and {@code build()} come from the linked-builder
 * base): the token domain and mint binder it declares are stored on the parent
 * authenticator (one per authenticator), so {@code .authorization(...)} delegates
 * to {@link AuthenticatorBuilder#tokenAuthorization} / {@link AuthenticatorBuilder#mintBinder}.
 */
@Reflected
public class AuthenticatorAuthentication<E> extends
        AbstractAutomaticLinkedBuilder<IAuthenticatorAuthenticationBuilder<E>, IAuthenticatorBuilder<E>, IAuthenticatorAuthenticationContext>
        implements IAuthenticatorAuthenticationBuilder<E> {

    private final AuthenticatorBuilder<E> authenticator;

    public AuthenticatorAuthentication(AuthenticatorBuilder<E> authenticator) {
        super(authenticator);
        this.authenticator = Objects.requireNonNull(authenticator, "Authenticator cannot be null");
    }

    @Override
    @SuppressWarnings("unchecked")
    public IAuthenticatorAuthorizationBuilder<E> authorization(IDomainBuilder<E> authorizationDomain) throws ApiException {
        return this.authenticator.tokenAuthorization(authorizationDomain);
    }

    @Override
    public IAuthenticatorMethodBinderBuilder<E> authorization(
            ISupplierBuilder<?, ? extends ISupplier<?>> supplier, String methodName) throws ApiException {
        return this.authenticator.mintBinder(supplier, methodName);
    }

    @Override
    protected synchronized IAuthenticatorAuthenticationContext doBuild() throws ApiException {
        // Navigation node: the declared token domain / mint binder live on the
        // authenticator, so there is no aggregated state to build here.
        return new AuthenticatorAuthenticationContext();
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // No-op: a navigation node has nothing to auto-detect.
    }

}

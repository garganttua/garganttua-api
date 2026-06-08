package com.garganttua.api.core.builder;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.Objects;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorAuthentication;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorAuthorizationBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorMethodBinderBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

/**
 * Per-authentication sub-builder of {@link AuthenticatorBuilder}. A thin
 * navigation object: the mint binder it declares is stored on the parent
 * authenticator (one mint per authenticator), so {@code .authorization(...)}
 * delegates to {@link AuthenticatorBuilder#mintBinder} and {@code .up()} returns
 * the authenticator.
 */
@Reflected
public class AuthenticatorAuthentication<E> implements IAuthenticatorAuthentication<E> {

    private final AuthenticatorBuilder<E> authenticator;

    public AuthenticatorAuthentication(AuthenticatorBuilder<E> authenticator) {
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
    public IAuthenticatorBuilder<E> up() {
        return this.authenticator;
    }

}

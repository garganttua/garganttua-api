package com.garganttua.api.commons.context.dsl.security;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.commons.security.context.IAuthenticatorAuthorizationContext;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.core.crypto.IKeyRealm;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public interface IAuthenticatorAuthorizationBuilder<E> extends IAutomaticLinkedBuilder<IAuthenticatorAuthorizationBuilder<E>, IAuthenticatorBuilder<E>, IAuthenticatorAuthorizationContext>{

    IAuthenticatorAuthorizationBuilder<E> lifeTime(int i, TimeUnit days);

    IAuthenticatorAuthorizationBuilder<E> refreshLifeTime(int i, TimeUnit days);

    /**
     * Mode <strong>persisté</strong>: declares a key domain (an entity marked
     * {@code @Key} or built with {@code .security().key()} on its domain builder)
     * as the backing store. The framework will auto-create the key on first
     * use and look it up on subsequent calls, scoping the visibility according
     * to {@link com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage}.
     *
     * <p>Returns the key sub-builder so the caller can configure
     * {@code .usage(...)}, {@code .algorithm(...)}, {@code .signatureAlgorithm(...)},
     * {@code .lifeTime(...)}.
     */
    IAuthenticatorAuthorizationKeyBuilder<E> key(IDomainBuilder<E> keyDomain);

    /**
     * Mode <strong>direct</strong>: wires a user-provided {@link IKeyRealm}
     * supplier (Vault, HSM, in-memory fixed realm for tests). The supplier
     * takes full responsibility for materializing the key — the framework
     * does not look at {@code usage} in this mode and does nothing extra.
     *
     * <p>Returns the parent builder: this mode has no sub-configuration —
     * the supplier knows what the key is.
     */
    IAuthenticatorAuthorizationBuilder<E> key(ISupplierBuilder<?, ? extends ISupplier<?>> keyRealmSupplier);

}

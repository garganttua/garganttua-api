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

    IAuthenticatorAuthorizationKeyBuilder<E> key(IDomainBuilder<E> key);

    /**
     * Wires the user-provided {@link IKeyRealm} supplier used by the signing /
     * verification stages of the pipeline. The API ships no {@code IKeyRealm}
     * implementation — callers inject one (e.g. from {@code garganttua-crypto}).
     */
    IAuthenticatorAuthorizationBuilder<E> keyRealm(ISupplierBuilder<? extends IKeyRealm, ? extends ISupplier<? extends IKeyRealm>> keyRealm);

}

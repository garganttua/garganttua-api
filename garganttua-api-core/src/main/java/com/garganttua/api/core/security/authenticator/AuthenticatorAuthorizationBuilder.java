package com.garganttua.api.core.security.authenticator;
import com.garganttua.api.core.api.ApiBuilder;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.security.authenticator.AuthenticatorAuthorizationContext;
import com.garganttua.api.commons.security.context.IAuthenticatorAuthorizationContext;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorAuthorizationBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorAuthorizationKeyBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorBuilder;
import com.garganttua.core.crypto.IKeyRealm;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;
import com.garganttua.api.commons.ApiException;

@Reflected
public class AuthenticatorAuthorizationBuilder<E> extends
        AbstractAutomaticLinkedBuilder<IAuthenticatorAuthorizationBuilder<E>, IAuthenticatorBuilder<E>, IAuthenticatorAuthorizationContext>
        implements IAuthenticatorAuthorizationBuilder<E> {

    private int duration;
    private TimeUnit unit;
    private int refreshDuration;
    private TimeUnit refreshUnit;
    private IDomainBuilder keyDomain;
    private AuthenticatorAuthorizationKeyBuilder authenticatorAuthorizationKey;
    private ISupplierBuilder<?, ? extends ISupplier<?>> keyRealm;
    private final IDomainBuilder authorizationDomainBuilder;

    public AuthenticatorAuthorizationBuilder(IAuthenticatorBuilder<E> authenticatorBuilder, IDomainBuilder authorizationDomainBuilder) {
        super(authenticatorBuilder);
        this.authorizationDomainBuilder = authorizationDomainBuilder;
    }

    @Override
    public IAuthenticatorAuthorizationBuilder<E> lifeTime(int duration, TimeUnit unit) {
        this.duration = duration;
        this.unit = Objects.requireNonNull(unit, "Unit cannot be null");
        return this;
    }

    @Override
    public IAuthenticatorAuthorizationBuilder<E> refreshLifeTime(int duration, TimeUnit unit) {
        this.refreshDuration = duration;
        this.refreshUnit = Objects.requireNonNull(unit, "Unit cannot be null");
        return this;
    }

    @Override
    public IAuthenticatorAuthorizationKeyBuilder<E> key(IDomainBuilder<E> keyDomain) {
        Objects.requireNonNull(keyDomain, "Key domain cannot be null");
        if (this.keyRealm != null) {
            throw new IllegalStateException(
                    "key(supplier) and key(domain) are mutually exclusive on the same authorization "
                            + "— supplier already declared");
        }
        this.keyDomain = keyDomain;
        return this.authenticatorAuthorizationKey = new AuthenticatorAuthorizationKeyBuilder(this, keyDomain);
    }

    @Override
    public IAuthenticatorAuthorizationBuilder<E> key(
            ISupplierBuilder<?, ? extends ISupplier<?>> keyRealmSupplier) {
        Objects.requireNonNull(keyRealmSupplier, "Key realm supplier cannot be null");
        if (this.keyDomain != null) {
            throw new IllegalStateException(
                    "key(supplier) and key(domain) are mutually exclusive on the same authorization "
                            + "— domain already declared");
        }
        this.keyRealm = keyRealmSupplier;
        return this;
    }

    @Override
    protected synchronized IAuthenticatorAuthorizationContext doBuild() throws ApiException {
        var keyContext = this.authenticatorAuthorizationKey != null
                ? (com.garganttua.api.commons.security.context.IAuthenticatorAuthorizationKeyContext) this.authenticatorAuthorizationKey.build()
                : null;
        // Store the authorization domain builder — it will be built later by ApiBuilder.
        // Validation (owned check) happens at runtime in CREATE_AUTHORIZATION.gs.
        return new AuthenticatorAuthorizationContext(
                this.duration, this.unit,
                this.refreshDuration, this.refreshUnit,
                keyContext, this.authorizationDomainBuilder,
                this.keyRealm);
    }

    @Override
    protected void doAutoDetection() throws ApiException {
    }

}

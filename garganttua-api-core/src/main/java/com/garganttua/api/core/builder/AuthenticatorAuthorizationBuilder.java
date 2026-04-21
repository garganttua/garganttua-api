package com.garganttua.api.core.builder;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.context.security.AuthenticatorAuthorizationContext;
import com.garganttua.api.commons.security.context.IAuthenticatorAuthorizationContext;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorAuthorizationBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorAuthorizationKeyBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.commons.ApiException;

public class AuthenticatorAuthorizationBuilder<E> extends
        AbstractAutomaticLinkedBuilder<IAuthenticatorAuthorizationBuilder<E>, IAuthenticatorBuilder<E>, IAuthenticatorAuthorizationContext>
        implements IAuthenticatorAuthorizationBuilder<E> {

    private int duration;
    private TimeUnit unit;
    private int refreshDuration;
    private TimeUnit refreshUnit;
    private IDomainBuilder keyDomain;
    private AuthenticatorAuthorizationKeyBuilder authenticatorAuthorizationKey;
    private final IDomainBuilder authorizationDomainBuilder;

    public AuthenticatorAuthorizationBuilder(IAuthenticatorBuilder<E> authenticatorBuilder, IDomainBuilder authorizationDomainBuilder) {
        super(authenticatorBuilder);
        this.authorizationDomainBuilder = authorizationDomainBuilder;
    }

    @Override
    public IAuthenticatorAuthorizationBuilder lifeTime(int duration, TimeUnit unit) {
        this.duration = duration;
        this.unit = Objects.requireNonNull(unit, "Unit cannot be null");
        return this;
    }

    @Override
    public IAuthenticatorAuthorizationBuilder refreshLifeTime(int duration, TimeUnit unit) {
        this.refreshDuration = duration;
        this.refreshUnit = Objects.requireNonNull(unit, "Unit cannot be null");
        return this;
    }

    @Override
    public IAuthenticatorAuthorizationKeyBuilder key(IDomainBuilder key) {
        this.keyDomain = Objects.requireNonNull(key, "Key cannot be null");
        return this.authenticatorAuthorizationKey = new AuthenticatorAuthorizationKeyBuilder(this, key);
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
                keyContext, this.authorizationDomainBuilder);
    }

    @Override
    protected void doAutoDetection() throws ApiException {
    }

}

package com.garganttua.api.core.builder;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.context.application.AuthenticatorAuthorizationContext;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IAuthenticatorAuthorizationBuilder;
import com.garganttua.api.spec.engine.IAuthenticatorAuthorizationContext;
import com.garganttua.api.spec.engine.IAuthenticatorAuthorizationKeyBuilder;
import com.garganttua.api.spec.engine.IAuthenticatorBuilder;
import com.garganttua.api.spec.engine.IDomainBuilder;

public class AuthenticatorAuthorizationBuilder extends
        AbstractAutomaticLinkedBuilder<IAuthenticatorAuthorizationContext, IAuthenticatorAuthorizationBuilder, IAuthenticatorBuilder>
        implements IAuthenticatorAuthorizationBuilder {

    private Integer duration;
    private TimeUnit unit;
    private Integer refreshDuration;
    private TimeUnit refreshUnit;
    private IDomainBuilder keyDomain;
    private AuthenticatorAuthorizationKeyBuilder authenticatorAuthorizationKey;

    public AuthenticatorAuthorizationBuilder(IAuthenticatorBuilder authenticatorBuilder) {
        super(authenticatorBuilder);
    }

    @Override
    public IAuthenticatorAuthorizationBuilder lifeTime(int duration, TimeUnit unit) {
        this.duration = Objects.requireNonNull(duration, "Duration cannot be null");
        this.unit = Objects.requireNonNull(unit, "Unit cannot be null");
        return this;
    }

    @Override
    public IAuthenticatorAuthorizationBuilder refreshLifeTime(int duration, TimeUnit unit) {
        this.refreshDuration = Objects.requireNonNull(duration, "Duration cannot be null");
        this.refreshUnit = Objects.requireNonNull(unit, "Unit cannot be null");
        return this;
    }

    @Override
    public IAuthenticatorAuthorizationKeyBuilder key(IDomainBuilder key) {
        this.keyDomain = Objects.requireNonNull(key, "Key cannot be null");
        return this.authenticatorAuthorizationKey = new AuthenticatorAuthorizationKeyBuilder(this, key);
    }

    @Override
    protected IAuthenticatorAuthorizationContext doBuild() throws CoreException {
        return new AuthenticatorAuthorizationContext(this.duration,
                this.unit,
                this.refreshDuration,
                this.refreshUnit,
                this.keyDomain.build(), this.authenticatorAuthorizationKey.build());
    }

    @Override
    protected void doAutoDetection() {

    }

}

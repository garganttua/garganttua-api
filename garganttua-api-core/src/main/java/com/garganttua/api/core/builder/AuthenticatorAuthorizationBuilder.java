package com.garganttua.api.core.builder;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.spec.context.IAuthenticatorAuthorizationContext;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticatorAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticatorAuthorizationKeyBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticatorBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.dsl.DslException;

public class AuthenticatorAuthorizationBuilder extends
        AbstractAutomaticLinkedBuilder<IAuthenticatorAuthorizationBuilder, IAuthenticatorBuilder, IAuthenticatorAuthorizationContext>
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
    protected IAuthenticatorAuthorizationContext doBuild() throws DslException {
        /* return new AuthenticatorAuthorizationContext(this.duration,
                this.unit,
                this.refreshDuration,
                this.refreshUnit,
                this.keyDomain.build(), this.authenticatorAuthorizationKey.build()); */
                 throw new UnsupportedOperationException("Unimplemented method 'doAutoDetection'");
    }

    @Override
    protected void doAutoDetection() throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doAutoDetection'");
    }

}

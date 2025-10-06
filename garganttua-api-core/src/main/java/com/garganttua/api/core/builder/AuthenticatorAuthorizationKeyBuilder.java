package com.garganttua.api.core.builder;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.garganttua.api.core.context.application.AuthenticatorAuthorizationKeyContext;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IAuthenticatorAuthorizationBuilder;
import com.garganttua.api.spec.engine.IAuthenticatorAuthorizationKeyBuilder;
import com.garganttua.api.spec.engine.IAuthenticatorAuthorizationKeyContext;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;

public class AuthenticatorAuthorizationKeyBuilder extends
        AbstractAutomaticLinkedBuilder<IAuthenticatorAuthorizationKeyContext, IAuthenticatorAuthorizationKeyBuilder, IAuthenticatorAuthorizationBuilder>
        implements IAuthenticatorAuthorizationKeyBuilder {

    private Integer duration;
    private TimeUnit unit;
    private AuthenticatorKeyUsage usage;
    private KeyAlgorithm algorithm;
    private SignatureAlgorithm signAlgorithm;
    private @Nonnull IDomainBuilder key;
    private boolean autoCreate = false;

    public AuthenticatorAuthorizationKeyBuilder(IAuthenticatorAuthorizationBuilder authenticatorAuthorizationBuilder,
            IDomainBuilder key) {
        super(authenticatorAuthorizationBuilder);
        this.key = Objects.requireNonNull(key, "Key cannot be null");
    }

    @Override
    public AuthenticatorAuthorizationKeyBuilder usage(AuthenticatorKeyUsage usage) {
        this.usage = Objects.requireNonNull(usage, "Usage cannot be null");
        return this;
    }

    @Override
    public AuthenticatorAuthorizationKeyBuilder algorithm(KeyAlgorithm algo) {
        this.algorithm = Objects.requireNonNull(algo, "Algorithm cannot be null");
        return this;
    }

    @Override
    public AuthenticatorAuthorizationKeyBuilder signatureAlgorithm(SignatureAlgorithm algo) {
        this.signAlgorithm = Objects.requireNonNull(algo, "Algorithm cannot be null");
        return this;
    }

    @Override
    public AuthenticatorAuthorizationKeyBuilder lifeTime(int duration, TimeUnit unit) {
        this.duration = Objects.requireNonNull(duration, "Duration cannot be null");
        this.unit = Objects.requireNonNull(unit, "Unit cannot be null");
        return this;
    }

    @Override
    protected IAuthenticatorAuthorizationKeyContext doBuild() throws CoreException {
        return new AuthenticatorAuthorizationKeyContext(this.duration,
                this.unit,
                this.usage,
                this.algorithm,
                this.signAlgorithm,
                this.key);
    }

    @Override
    protected void doAutoDetection() throws CoreException {

    }

    @Override
    public IAuthenticatorAuthorizationKeyBuilder autoCreate(boolean b) {
        this.autoCreate = b;
        return this;
    }
}

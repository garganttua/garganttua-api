package com.garganttua.api.core.security.authenticator;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.security.authenticator.AuthenticatorAuthorizationKeyContext;
import com.garganttua.api.commons.security.context.IAuthenticatorAuthorizationKeyContext;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorAuthorizationBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorAuthorizationKeyBuilder;
import com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.core.crypto.IKeyAlgorithm;
import com.garganttua.core.crypto.SignatureAlgorithm;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;

@Reflected
public class AuthenticatorAuthorizationKeyBuilder<E> extends
        AbstractAutomaticLinkedBuilder<IAuthenticatorAuthorizationKeyBuilder<E>, IAuthenticatorAuthorizationBuilder<E>, IAuthenticatorAuthorizationKeyContext>
        implements IAuthenticatorAuthorizationKeyBuilder<E> {

    private Integer duration;
    private TimeUnit unit;
    private AuthenticatorKeyUsage usage;
    private IKeyAlgorithm algorithm;
    private SignatureAlgorithm signAlgorithm;
    private IDomainBuilder key;
    private boolean autoGenerate = true;
    private boolean autoRotate = false;

    public AuthenticatorAuthorizationKeyBuilder(IAuthenticatorAuthorizationBuilder<E> authenticatorAuthorizationBuilder,
            IDomainBuilder<E> key) {
        super(authenticatorAuthorizationBuilder);
        this.key = Objects.requireNonNull(key, "Key cannot be null");
    }

    @Override
    public AuthenticatorAuthorizationKeyBuilder<E> usage(AuthenticatorKeyUsage usage) {
        this.usage = Objects.requireNonNull(usage, "Usage cannot be null");
        return this;
    }

    @Override
    public AuthenticatorAuthorizationKeyBuilder<E> algorithm(IKeyAlgorithm algo) {
        this.algorithm = Objects.requireNonNull(algo, "Algorithm cannot be null");
        return this;
    }

    @Override
    public AuthenticatorAuthorizationKeyBuilder<E> signatureAlgorithm(SignatureAlgorithm algo) {
        this.signAlgorithm = Objects.requireNonNull(algo, "Algorithm cannot be null");
        return this;
    }

    @Override
    public AuthenticatorAuthorizationKeyBuilder<E> lifeTime(int duration, TimeUnit unit) {
        this.duration = duration;
        this.unit = Objects.requireNonNull(unit, "Unit cannot be null");
        return this;
    }

    @Override
    public AuthenticatorAuthorizationKeyBuilder<E> autoGenerate(boolean enabled) {
        this.autoGenerate = enabled;
        return this;
    }

    @Override
    public AuthenticatorAuthorizationKeyBuilder<E> autoRotate(boolean enabled) {
        this.autoRotate = enabled;
        return this;
    }

    @Override
    protected synchronized IAuthenticatorAuthorizationKeyContext doBuild() throws ApiException {
        return new AuthenticatorAuthorizationKeyContext(
                this.duration != null ? this.duration : 0,
                this.unit,
                this.usage,
                this.algorithm,
                this.signAlgorithm,
                this.key,
                this.autoGenerate,
                this.autoRotate);
    }

    @Override
    protected void doAutoDetection() throws ApiException {

    }
}

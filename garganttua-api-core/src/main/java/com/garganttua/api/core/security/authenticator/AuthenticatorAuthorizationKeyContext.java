package com.garganttua.api.core.security.authenticator;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.security.authenticator.DomainAuthenticatorAuthorizationKeyDefinition;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.security.context.IAuthenticatorAuthorizationKeyContext;
import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationKeyDefinition;
import com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.core.crypto.IKeyAlgorithm;
import com.garganttua.core.crypto.SignatureAlgorithm;

public class AuthenticatorAuthorizationKeyContext implements IAuthenticatorAuthorizationKeyContext {

    private final DomainAuthenticatorAuthorizationKeyDefinition authenticatorAuthorizationKeyDefinition;

    public AuthenticatorAuthorizationKeyContext(int duration, TimeUnit unit, AuthenticatorKeyUsage usage,
            IKeyAlgorithm algorithm, SignatureAlgorithm signAlgorithm, IDomainBuilder<?> keyDomain,
            boolean autoGenerate, boolean autoRotate) {
        this.authenticatorAuthorizationKeyDefinition = new DomainAuthenticatorAuthorizationKeyDefinition(
                usage, algorithm, signAlgorithm, duration, unit, keyDomain, autoGenerate, autoRotate);
    }

    @Override
    public IDomainAuthenticatorAuthorizationKeyDefinition getAuthenticatorAuthorizationKeyDefinition() {
        return this.authenticatorAuthorizationKeyDefinition;
    }

}

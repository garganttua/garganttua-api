package com.garganttua.api.core.context.security;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.definition.DomainAuthenticatorAuthorizationKeyDefinition;
import com.garganttua.api.spec.security.context.IAuthenticatorAuthorizationKeyContext;
import com.garganttua.api.spec.definition.IDomainAuthenticatorAuthorizationKeyDefinition;
import com.garganttua.api.spec.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;

public class AuthenticatorAuthorizationKeyContext implements IAuthenticatorAuthorizationKeyContext {

    private final DomainAuthenticatorAuthorizationKeyDefinition authenticatorAuthorizationKeyDefinition;

    public AuthenticatorAuthorizationKeyContext(int duration, TimeUnit unit, AuthenticatorKeyUsage usage,
            KeyAlgorithm algorithm, SignatureAlgorithm signAlgorithm) {
        this.authenticatorAuthorizationKeyDefinition = new DomainAuthenticatorAuthorizationKeyDefinition(
                usage, algorithm, signAlgorithm, duration, unit);
    }

    @Override
    public IDomainAuthenticatorAuthorizationKeyDefinition getAuthenticatorAuthorizationKeyDefinition() {
        return this.authenticatorAuthorizationKeyDefinition;
    }

}

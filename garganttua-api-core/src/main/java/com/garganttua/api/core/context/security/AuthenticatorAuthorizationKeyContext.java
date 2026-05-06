package com.garganttua.api.core.context.security;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.definition.DomainAuthenticatorAuthorizationKeyDefinition;
import com.garganttua.api.commons.security.context.IAuthenticatorAuthorizationKeyContext;
import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationKeyDefinition;
import com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.core.crypto.IKeyAlgorithm;
import com.garganttua.core.crypto.SignatureAlgorithm;

public class AuthenticatorAuthorizationKeyContext implements IAuthenticatorAuthorizationKeyContext {

    private final DomainAuthenticatorAuthorizationKeyDefinition authenticatorAuthorizationKeyDefinition;

    public AuthenticatorAuthorizationKeyContext(int duration, TimeUnit unit, AuthenticatorKeyUsage usage,
            IKeyAlgorithm algorithm, SignatureAlgorithm signAlgorithm) {
        this.authenticatorAuthorizationKeyDefinition = new DomainAuthenticatorAuthorizationKeyDefinition(
                usage, algorithm, signAlgorithm, duration, unit);
    }

    @Override
    public IDomainAuthenticatorAuthorizationKeyDefinition getAuthenticatorAuthorizationKeyDefinition() {
        return this.authenticatorAuthorizationKeyDefinition;
    }

}

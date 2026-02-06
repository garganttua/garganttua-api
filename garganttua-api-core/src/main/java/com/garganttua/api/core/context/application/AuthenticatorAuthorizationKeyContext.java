package com.garganttua.api.core.context.application;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.definition.DomainAuthenticatorAuthorizationKeyDefinition;
import com.garganttua.api.spec.context.IAuthenticatorAuthorizationKeyContext;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.definition.IDomainAuthenticatorAuthorizationKeyDefinition;
import com.garganttua.api.spec.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;

public class AuthenticatorAuthorizationKeyContext implements IAuthenticatorAuthorizationKeyContext {

    private DomainAuthenticatorAuthorizationKeyDefinition authenticatorAuthorizationKeyDefinition;

    public AuthenticatorAuthorizationKeyContext(Integer duration, TimeUnit unit, AuthenticatorKeyUsage usage,
            KeyAlgorithm algorithm, SignatureAlgorithm signAlgorithm, IDomainBuilder key) {
        this.authenticatorAuthorizationKeyDefinition = new DomainAuthenticatorAuthorizationKeyDefinition();
    }

    @Override
    public IDomainAuthenticatorAuthorizationKeyDefinition getAuthenticatorAuthorizationKeyDefinition() {
        return this.authenticatorAuthorizationKeyDefinition;
    }

}

package com.garganttua.api.core.context.security;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.definition.DomainAuthenticatorAuthorizationDefinition;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.security.context.IAuthenticatorAuthorizationContext;
import com.garganttua.api.commons.security.context.IAuthenticatorAuthorizationKeyContext;
import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationDefinition;

public class AuthenticatorAuthorizationContext implements IAuthenticatorAuthorizationContext {

    private final DomainAuthenticatorAuthorizationDefinition authenticatorAuthorizationDefinition;

    public AuthenticatorAuthorizationContext(int duration, TimeUnit unit, int refreshDuration,
            TimeUnit refreshUnit, IAuthenticatorAuthorizationKeyContext keyContext,
            IDomainBuilder<?> authorizationDomainBuilder) {
        this.authenticatorAuthorizationDefinition = new DomainAuthenticatorAuthorizationDefinition(
                duration, unit, refreshDuration, refreshUnit,
                keyContext != null ? keyContext.getAuthenticatorAuthorizationKeyDefinition() : null,
                authorizationDomainBuilder);
    }

    @Override
    public IDomainAuthenticatorAuthorizationDefinition getAuthenticatorAuthorizationDefinition() {
        return this.authenticatorAuthorizationDefinition;
    }

}

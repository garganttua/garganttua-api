package com.garganttua.api.core.context.security;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.definition.DomainAuthenticatorAuthorizationDefinition;
import com.garganttua.api.spec.security.context.IAuthenticatorAuthorizationContext;
import com.garganttua.api.spec.security.context.IAuthenticatorAuthorizationKeyContext;
import com.garganttua.api.spec.definition.IDomainAuthenticatorAuthorizationDefinition;
import com.garganttua.api.spec.context.IDomainContext;

public class AuthenticatorAuthorizationContext implements IAuthenticatorAuthorizationContext {

    private DomainAuthenticatorAuthorizationDefinition authenticatorAuthorizationDefinition;

    public AuthenticatorAuthorizationContext(Integer duration, TimeUnit unit, Integer refreshDuration,
            TimeUnit refreshUnit, IDomainContext<?> iDomainContext,
            IAuthenticatorAuthorizationKeyContext iAuthenticatorAuthorizationKeyContext) {
        this.authenticatorAuthorizationDefinition = new DomainAuthenticatorAuthorizationDefinition();
    }

    @Override
    public IDomainAuthenticatorAuthorizationDefinition getAuthenticatorAuthorizationDefinition() {
        return this.authenticatorAuthorizationDefinition;
    }

}

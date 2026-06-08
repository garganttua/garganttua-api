package com.garganttua.api.core.security.authenticator;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.security.authenticator.DomainAuthenticatorAuthorizationDefinition;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.security.context.IAuthenticatorAuthorizationContext;
import com.garganttua.api.commons.security.context.IAuthenticatorAuthorizationKeyContext;
import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationDefinition;
import com.garganttua.core.crypto.IKeyRealm;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public class AuthenticatorAuthorizationContext implements IAuthenticatorAuthorizationContext {

    private final DomainAuthenticatorAuthorizationDefinition authenticatorAuthorizationDefinition;

    public AuthenticatorAuthorizationContext(int duration, TimeUnit unit, int refreshDuration,
            TimeUnit refreshUnit, IAuthenticatorAuthorizationKeyContext keyContext,
            IDomainBuilder<?> authorizationDomainBuilder,
            ISupplierBuilder<?, ? extends ISupplier<?>> keyRealm) {
        this.authenticatorAuthorizationDefinition = new DomainAuthenticatorAuthorizationDefinition(
                duration, unit, refreshDuration, refreshUnit,
                keyContext != null ? keyContext.getAuthenticatorAuthorizationKeyDefinition() : null,
                authorizationDomainBuilder, keyRealm);
    }

    @Override
    public IDomainAuthenticatorAuthorizationDefinition getAuthenticatorAuthorizationDefinition() {
        return this.authenticatorAuthorizationDefinition;
    }

}

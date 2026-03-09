package com.garganttua.api.core.context.security;

import java.util.List;

import com.garganttua.api.core.definition.DomainAuthenticatorDefintion;
import com.garganttua.api.spec.security.context.IAuthenticationContext;
import com.garganttua.api.spec.security.context.IAuthenticatorAuthorizationContext;
import com.garganttua.api.spec.security.context.IAuthenticatorContext;
import com.garganttua.api.spec.definition.IDomainAuthenticatorDefinition;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.core.reflection.ObjectAddress;

public class AuthenticatorContext implements IAuthenticatorContext {

    private DomainAuthenticatorDefintion authenticatorDefinition;

    public AuthenticatorContext(boolean alwaysEnabled, ObjectAddress login, ObjectAddress authorities,
            ObjectAddress credentialsNonExpired, ObjectAddress enabled, ObjectAddress accountNonLocked,
            ObjectAddress accountNonExpired, AuthenticatorScope scope, List<IAuthenticationContext> collect,
            IAuthenticatorAuthorizationContext iAuthenticatorAuthorizationContext) {
        this.authenticatorDefinition = new DomainAuthenticatorDefintion();
    }

    @Override
    public IDomainAuthenticatorDefinition getAuthenticatorDefinition() {
        return this.authenticatorDefinition;
    }

}

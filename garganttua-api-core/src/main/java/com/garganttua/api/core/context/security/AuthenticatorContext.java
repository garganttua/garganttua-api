package com.garganttua.api.core.context.security;

import com.garganttua.api.spec.definition.IAuthenticatorDefinition;
import com.garganttua.api.spec.security.context.IAuthenticatorContext;

public class AuthenticatorContext implements IAuthenticatorContext {

    private IAuthenticatorDefinition authenticatorDefinition;

    public AuthenticatorContext(IAuthenticatorDefinition authenticatorDefinition) {
        this.authenticatorDefinition = authenticatorDefinition;
    }

    @Override
    public IAuthenticatorDefinition getAuthenticatorDefinition() {
        return this.authenticatorDefinition;
    }

}

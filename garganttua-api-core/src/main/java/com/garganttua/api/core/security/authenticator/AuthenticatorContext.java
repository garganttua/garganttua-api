package com.garganttua.api.core.security.authenticator;

import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.security.context.IAuthenticatorContext;

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

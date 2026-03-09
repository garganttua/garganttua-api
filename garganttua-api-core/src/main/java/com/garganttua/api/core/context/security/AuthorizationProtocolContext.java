package com.garganttua.api.core.context.security;

import com.garganttua.api.core.definition.AuthorizationProtocolDefinition;
import com.garganttua.api.spec.security.context.IAuthorizationProtocolContext;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationProtocolMethodBinderBuilder;
import com.garganttua.api.spec.definition.IAuthorizationProtocolDefinition;

public class AuthorizationProtocolContext implements IAuthorizationProtocolContext {

    private AuthorizationProtocolDefinition authorizationProtocolDefinition;

    public AuthorizationProtocolContext(IAuthorizationProtocolMethodBinderBuilder setAuthorization,
            IAuthorizationProtocolMethodBinderBuilder getAuthorization) {
        this.authorizationProtocolDefinition = new AuthorizationProtocolDefinition();
    }

    @Override
    public IAuthorizationProtocolDefinition getAuthorizationProtocolDefinition() {
        return this.authorizationProtocolDefinition;
    }

}

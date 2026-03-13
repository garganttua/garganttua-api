package com.garganttua.api.core.context.security;

import java.util.List;
import java.util.Objects;

import com.garganttua.api.core.definition.AuthenticationDefinition;
import com.garganttua.api.core.security.authentication.AuthenticationRequestBuilder;
import com.garganttua.api.spec.definition.IAuthenticationDefinition;
import com.garganttua.api.spec.security.IApiSecurityContext;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequestBuilder;
import com.garganttua.api.spec.security.context.IAuthenticationContext;

public class AuthenticationContext implements IAuthenticationContext {

    private AuthenticationDefinition authenticationDefinition;
    private IApiSecurityContext apiSecurityContext;

    public AuthenticationContext(AuthenticationDefinition definition) {
        this.authenticationDefinition = Objects.requireNonNull(definition, "Authentication definition is mandatory to create an authentication context");
    }

    public void setApiSecurityContext(IApiSecurityContext apiSecurityContext) {
        this.apiSecurityContext = apiSecurityContext;
    }

    @Override
    public IAuthenticationDefinition getAuthenticationDefinition() {
        return this.authenticationDefinition;
    }

    @Override
    public IAuthenticationRequestBuilder request() {
        return new AuthenticationRequestBuilder(this.apiSecurityContext, List.of(this));
    }

}

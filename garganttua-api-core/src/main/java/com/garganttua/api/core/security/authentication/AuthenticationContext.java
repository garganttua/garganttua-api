package com.garganttua.api.core.security.authentication;

import java.util.Objects;

import com.garganttua.api.core.security.authentication.AuthenticationDefinition;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.definition.IAuthenticationDefinition;
import com.garganttua.api.commons.security.context.IAuthenticationContext;


public class AuthenticationContext implements IAuthenticationContext {

    private AuthenticationDefinition authenticationDefinition;
    private IDomain<?> domainContext;

    public AuthenticationContext(AuthenticationDefinition definition) {
        this.authenticationDefinition = Objects.requireNonNull(definition, "Authentication definition is mandatory to create an authentication context");
    }

    public void setDomain(IDomain<?> domainContext) {
        this.domainContext = domainContext;
    }

    public IDomain<?> getDomain() {
        return this.domainContext;
    }

    @Override
    public IAuthenticationDefinition getAuthenticationDefinition() {
        return this.authenticationDefinition;
    }

}

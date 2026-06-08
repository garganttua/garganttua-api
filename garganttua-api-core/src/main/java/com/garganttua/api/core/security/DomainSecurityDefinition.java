package com.garganttua.api.core.security;

import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.definition.IDomainAuthorizationDefinition;
import com.garganttua.api.commons.definition.IDomainSecurityDefinition;

public record DomainSecurityDefinition(
    boolean disabled,
    IAuthenticatorDefinition authenticatorDefinition,
    IDomainAuthorizationDefinition authorizationDefinition) implements IDomainSecurityDefinition {

}

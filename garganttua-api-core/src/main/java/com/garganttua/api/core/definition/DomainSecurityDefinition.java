package com.garganttua.api.core.definition;

import com.garganttua.api.spec.definition.IAuthenticatorDefinition;
import com.garganttua.api.spec.definition.IDomainAuthorizationDefinition;
import com.garganttua.api.spec.definition.IDomainSecurityDefinition;

public record DomainSecurityDefinition(
    boolean disabled,
    IAuthenticatorDefinition authenticatorDefinition,
    IDomainAuthorizationDefinition authorizationDefinition) implements IDomainSecurityDefinition {

}

package com.garganttua.api.spec.definition;

public interface IDomainSecurityDefinition {

    boolean disabled();

    IAuthenticatorDefinition authenticatorDefinition();

    IDomainAuthorizationDefinition authorizationDefinition();

}

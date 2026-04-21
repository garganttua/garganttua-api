package com.garganttua.api.commons.definition;

public interface IDomainSecurityDefinition {

    boolean disabled();

    IAuthenticatorDefinition authenticatorDefinition();

    IDomainAuthorizationDefinition authorizationDefinition();

}

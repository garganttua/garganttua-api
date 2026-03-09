package com.garganttua.api.spec.security.context;

import com.garganttua.api.spec.definition.IDomainAuthenticatorAuthorizationDefinition;

public interface IAuthenticatorAuthorizationContext {

    IDomainAuthenticatorAuthorizationDefinition getAuthenticatorAuthorizationDefinition();

}

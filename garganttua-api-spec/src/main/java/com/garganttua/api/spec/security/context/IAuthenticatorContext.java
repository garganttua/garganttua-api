package com.garganttua.api.spec.security.context;

import com.garganttua.api.spec.definition.IDomainAuthenticatorDefinition;

public interface IAuthenticatorContext {

    IDomainAuthenticatorDefinition getAuthenticatorDefinition();

}

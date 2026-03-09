package com.garganttua.api.spec.security.context;

import com.garganttua.api.spec.definition.IDomainAuthorizationDefinition;

public interface IAuthorizationContext {

    IDomainAuthorizationDefinition getAuthorizationDefinition();

}

package com.garganttua.api.commons.security.context;

import com.garganttua.api.commons.definition.IDomainAuthorizationDefinition;

public interface IAuthorizationContext {

    IDomainAuthorizationDefinition getAuthorizationDefinition();

}

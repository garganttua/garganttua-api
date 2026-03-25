package com.garganttua.api.core.definition;

import java.util.List;

import com.garganttua.api.spec.definition.IAuthenticationDefinition;
import com.garganttua.api.spec.definition.IAuthenticatorDefinition;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.core.reflection.ObjectAddress;

public record AuthenticatorDefintion(
        boolean alwaysEnabled,
        ObjectAddress login,
        ObjectAddress authorities,
        ObjectAddress credentialsNonExpired,
        ObjectAddress enabled,
        ObjectAddress accountNonLocked,
        ObjectAddress accountNonExpired,
        AuthenticatorScope scope,
        List<IAuthenticationDefinition> authenticationDefinitions) implements IAuthenticatorDefinition {

}

package com.garganttua.api.core.definition;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

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
        Map<Annotation, ObjectAddress> requiredAuthenticationFields,
        List<IAuthenticationDefinition> authenticationDefinitions) implements IAuthenticatorDefinition {

}

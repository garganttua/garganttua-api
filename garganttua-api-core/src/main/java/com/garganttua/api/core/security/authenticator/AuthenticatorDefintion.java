package com.garganttua.api.core.security.authenticator;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import com.garganttua.api.commons.definition.IAuthenticationDefinition;
import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationDefinition;
import com.garganttua.api.commons.security.authenticator.AuthenticatorScope;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IMethodBinder;

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
        List<IAuthenticationDefinition> authenticationDefinitions,
        IDomainAuthenticatorAuthorizationDefinition authorizationDefinition,
        IMethodBinder<?> authorizationMethodBinder) implements IAuthenticatorDefinition {

}

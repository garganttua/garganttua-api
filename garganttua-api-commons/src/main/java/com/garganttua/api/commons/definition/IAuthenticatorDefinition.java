package com.garganttua.api.commons.definition;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import com.garganttua.api.commons.security.authenticator.AuthenticatorScope;
import com.garganttua.core.reflection.ObjectAddress;

public interface IAuthenticatorDefinition {

    boolean alwaysEnabled();

    ObjectAddress login();

    ObjectAddress authorities();

    ObjectAddress credentialsNonExpired();

    ObjectAddress enabled();

    ObjectAddress accountNonLocked();

    ObjectAddress accountNonExpired();

    AuthenticatorScope scope();

    Map<Annotation, ObjectAddress> requiredAuthenticationFields();

    List<IAuthenticationDefinition> authenticationDefinitions();

    IDomainAuthenticatorAuthorizationDefinition authorizationDefinition();

}

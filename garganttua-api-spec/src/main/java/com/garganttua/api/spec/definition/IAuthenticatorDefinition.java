package com.garganttua.api.spec.definition;

import java.util.List;

import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
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

    List<IAuthenticationDefinition> authenticationDefinitions();

}

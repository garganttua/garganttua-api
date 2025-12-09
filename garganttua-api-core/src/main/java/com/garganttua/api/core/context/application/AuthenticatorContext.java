package com.garganttua.api.core.context.application;

import java.util.List;

import com.garganttua.api.spec.context.IAuthenticationContext;
import com.garganttua.api.spec.context.IAuthenticatorAuthorizationContext;
import com.garganttua.api.spec.context.IAuthenticatorContext;
import com.garganttua.api.spec.context.dsl.security.IAuthenticationBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticatorAuthorizationBuilder;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.core.reflection.ObjectAddress;

public class AuthenticatorContext implements IAuthenticatorContext {

    public AuthenticatorContext(boolean alwaysEnabled, ObjectAddress login, ObjectAddress authorities,
            ObjectAddress credentialsNonExpired, ObjectAddress enabled, ObjectAddress accountNonLocked,
            ObjectAddress accountNonExpired, AuthenticatorScope scope, List<IAuthenticationContext> collect,
            IAuthenticatorAuthorizationContext iAuthenticatorAuthorizationContext) {
        //TODO Auto-generated constructor stub
    }



}

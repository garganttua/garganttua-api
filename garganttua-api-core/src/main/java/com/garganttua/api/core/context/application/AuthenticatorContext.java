package com.garganttua.api.core.context.application;

import java.util.List;

import com.garganttua.api.spec.engine.IAuthenticationContext;
import com.garganttua.api.spec.engine.IAuthenticatorAuthorizationContext;
import com.garganttua.api.spec.engine.IAuthenticatorContext;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.reflection.GGObjectAddress;

public class AuthenticatorContext implements IAuthenticatorContext {

    public AuthenticatorContext(boolean alwaysEnabled, GGObjectAddress login, GGObjectAddress authorities,
            GGObjectAddress credentialsNonExpired, GGObjectAddress enabled, GGObjectAddress accountNonLocked,
            GGObjectAddress accountNonExpired, AuthenticatorScope scope, List<IAuthenticationContext> collect,
            IAuthenticatorAuthorizationContext iAuthenticatorAuthorizationContext) {
        //TODO Auto-generated constructor stub
    }

}

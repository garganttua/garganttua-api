package com.garganttua.api.core.context.application;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.spec.context.IAuthenticatorAuthorizationContext;
import com.garganttua.api.spec.context.IAuthenticatorAuthorizationKeyContext;

public class AuthenticatorAuthorizationContext implements IAuthenticatorAuthorizationContext {

    public AuthenticatorAuthorizationContext(Integer duration, TimeUnit unit, Integer refreshDuration,
            TimeUnit refreshUnit, IDomain iDomainContext,
            IAuthenticatorAuthorizationKeyContext iAuthenticatorAuthorizationKeyContext) {
        //TODO Auto-generated constructor stub
    }

}

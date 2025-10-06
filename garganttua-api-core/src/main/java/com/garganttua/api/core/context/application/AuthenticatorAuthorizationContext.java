package com.garganttua.api.core.context.application;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.spec.engine.IAuthenticatorAuthorizationContext;
import com.garganttua.api.spec.engine.IAuthenticatorAuthorizationKeyContext;
import com.garganttua.api.spec.engine.IDomainContext;

public class AuthenticatorAuthorizationContext implements IAuthenticatorAuthorizationContext {

    public AuthenticatorAuthorizationContext(Integer duration, TimeUnit unit, Integer refreshDuration,
            TimeUnit refreshUnit, IDomainContext iDomainContext,
            IAuthenticatorAuthorizationKeyContext iAuthenticatorAuthorizationKeyContext) {
        //TODO Auto-generated constructor stub
    }

}

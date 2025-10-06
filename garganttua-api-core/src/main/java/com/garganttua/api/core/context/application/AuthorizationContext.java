package com.garganttua.api.core.context.application;

import com.garganttua.api.spec.engine.IAuthorizationContext;
import com.garganttua.api.spec.engine.IAuthorizationMethodBinderBuilder;
import com.garganttua.reflection.GGObjectAddress;

public class AuthorizationContext implements IAuthorizationContext {

    public AuthorizationContext(GGObjectAddress type, GGObjectAddress revoked, GGObjectAddress creation,
            GGObjectAddress expiration, GGObjectAddress authorities, IAuthorizationMethodBinderBuilder toByteArray,
            IAuthorizationMethodBinderBuilder validate, IAuthorizationMethodBinderBuilder validateAgainst,
            IAuthorizationMethodBinderBuilder fromByteArray, Object signable,
            Object refreshable, boolean storable) {
        //TODO Auto-generated constructor stub
    }

}

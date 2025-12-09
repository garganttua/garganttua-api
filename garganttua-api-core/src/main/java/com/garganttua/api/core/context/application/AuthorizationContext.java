package com.garganttua.api.core.context.application;

import com.garganttua.api.spec.context.IAuthorizationContext;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationMethodBinderBuilder;
import com.garganttua.core.reflection.ObjectAddress;

public class AuthorizationContext implements IAuthorizationContext {

    public AuthorizationContext(ObjectAddress type, ObjectAddress revoked, ObjectAddress creation,
            ObjectAddress expiration, ObjectAddress authorities, IAuthorizationMethodBinderBuilder toByteArray,
            IAuthorizationMethodBinderBuilder validate, IAuthorizationMethodBinderBuilder validateAgainst,
            IAuthorizationMethodBinderBuilder fromByteArray, Object signable,
            Object refreshable, boolean storable) {
        //TODO Auto-generated constructor stub
    }

}

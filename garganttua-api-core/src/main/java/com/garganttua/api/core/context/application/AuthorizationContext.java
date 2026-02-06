package com.garganttua.api.core.context.application;

import com.garganttua.api.core.definition.DomainAuthorizationDefinition;
import com.garganttua.api.spec.context.IAuthorizationContext;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationMethodBinderBuilder;
import com.garganttua.api.spec.definition.IDomainAuthorizationDefinition;
import com.garganttua.core.reflection.ObjectAddress;

public class AuthorizationContext implements IAuthorizationContext {

    private DomainAuthorizationDefinition authorizationDefinition;

    public AuthorizationContext(ObjectAddress type, ObjectAddress revoked, ObjectAddress creation,
            ObjectAddress expiration, ObjectAddress authorities, IAuthorizationMethodBinderBuilder toByteArray,
            IAuthorizationMethodBinderBuilder validate, IAuthorizationMethodBinderBuilder validateAgainst,
            IAuthorizationMethodBinderBuilder fromByteArray, Object signable,
            Object refreshable, boolean storable) {
        this.authorizationDefinition = new DomainAuthorizationDefinition();
    }

    @Override
    public IDomainAuthorizationDefinition getAuthorizationDefinition() {
        return this.authorizationDefinition;
    }

}

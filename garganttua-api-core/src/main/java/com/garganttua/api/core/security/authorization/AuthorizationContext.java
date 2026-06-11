package com.garganttua.api.core.security.authorization;

import com.garganttua.api.core.security.authorization.DomainAuthorizationDefinition;
import com.garganttua.api.commons.security.context.IAuthorizationContext;
import com.garganttua.api.commons.definition.IDomainAuthorizationDefinition;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IMethodBinder;

public class AuthorizationContext implements IAuthorizationContext {

    private final DomainAuthorizationDefinition authorizationDefinition;

    public AuthorizationContext(ObjectAddress type, ObjectAddress authorities,
            ObjectAddress expiration, ObjectAddress creation, ObjectAddress revoked,
            boolean storable, boolean signable, boolean refreshable,
            ObjectAddress signatureField, ObjectAddress getDataToSignMethod,
            ObjectAddress refreshExpiration, ObjectAddress refreshRevoked,
            ObjectAddress encodeMethod, ObjectAddress decodeMethod,
            ObjectAddress signedBy, IMethodBinder<?> reconcileBinder) {
        this.authorizationDefinition = new DomainAuthorizationDefinition(
                type, authorities, expiration, creation, revoked,
                storable, signable, refreshable,
                signatureField, getDataToSignMethod,
                refreshExpiration, refreshRevoked,
                encodeMethod, decodeMethod, signedBy, reconcileBinder);
    }

    @Override
    public IDomainAuthorizationDefinition getAuthorizationDefinition() {
        return this.authorizationDefinition;
    }

}

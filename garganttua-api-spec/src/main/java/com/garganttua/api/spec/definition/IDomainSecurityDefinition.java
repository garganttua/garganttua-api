package com.garganttua.api.spec.definition;

import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;

public interface IDomainSecurityDefinition {

    boolean disabled();

    AuthenticatorInfos authenticatorInfos();

}

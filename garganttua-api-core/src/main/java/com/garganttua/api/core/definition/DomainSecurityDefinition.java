package com.garganttua.api.core.definition;

import com.garganttua.api.spec.definition.IDomainSecurityDefinition;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;

public record DomainSecurityDefinition(
    boolean disabled,
    AuthenticatorInfos authenticatorInfos) implements IDomainSecurityDefinition {

}

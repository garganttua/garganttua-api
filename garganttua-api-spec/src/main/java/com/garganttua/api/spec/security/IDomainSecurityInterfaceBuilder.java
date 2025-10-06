package com.garganttua.api.spec.security;

import com.garganttua.api.spec.engine.IAuthorizationProtocolBuilder;

public interface IDomainSecurityInterfaceBuilder {

    IDomainSecurityInterfaceBuilder authorizationProtocol(
            IAuthorizationProtocolBuilder authorizationProtocol);

}

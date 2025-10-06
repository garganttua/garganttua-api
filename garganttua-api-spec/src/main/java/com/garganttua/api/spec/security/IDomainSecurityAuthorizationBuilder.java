package com.garganttua.api.spec.security;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IAuthorizationProtocolBuilder;
import com.garganttua.api.spec.engine.IAutomaticLinkedBuilder;
import com.garganttua.api.spec.interfasse.IInterface;

public interface IDomainSecurityAuthorizationBuilder extends IAutomaticLinkedBuilder<Object, IDomainSecurityBuilder, IDomainSecurityAuthorizationBuilder>{

    IDomainSecurityAuthorizationBuilder interfasse(Class<? extends IInterface> interfaceClass) throws CoreException;

    IDomainSecurityAuthorizationBuilder protocol(IAuthorizationProtocolBuilder protocol);

}

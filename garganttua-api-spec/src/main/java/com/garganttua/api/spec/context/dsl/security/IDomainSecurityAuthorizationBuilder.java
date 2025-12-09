package com.garganttua.api.spec.context.dsl.security;

import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;

public interface IDomainSecurityAuthorizationBuilder<E> extends IAutomaticLinkedBuilder<IDomainSecurityAuthorizationBuilder<E>, IDomainSecurityBuilder<E>, Object>{

    IDomainSecurityAuthorizationBuilder<E> interfasse(Class<? extends IInterface> interfaceClass) throws DslException;

    IDomainSecurityAuthorizationBuilder<E> protocol(IAuthorizationProtocolBuilder protocol);

}

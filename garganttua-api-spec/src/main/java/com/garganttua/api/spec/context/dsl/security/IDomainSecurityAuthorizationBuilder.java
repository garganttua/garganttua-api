package com.garganttua.api.spec.context.dsl.security;

import com.garganttua.api.spec.endpoint.IEndpoint;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IClass;

public interface IDomainSecurityAuthorizationBuilder<E> extends IAutomaticLinkedBuilder<IDomainSecurityAuthorizationBuilder<E>, IDomainSecurityBuilder<E>, Object>{

    IDomainSecurityAuthorizationBuilder<E> interfasse(IClass<? extends IEndpoint> interfaceClass) throws ApiException;

    IDomainSecurityAuthorizationBuilder<E> protocol(IAuthorizationProtocolBuilder protocol);

}

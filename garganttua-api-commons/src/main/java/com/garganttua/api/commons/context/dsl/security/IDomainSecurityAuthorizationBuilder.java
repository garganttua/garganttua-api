package com.garganttua.api.commons.context.dsl.security;

import com.garganttua.api.commons.endpoint.IEndpoint;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IClass;

public interface IDomainSecurityAuthorizationBuilder<E> extends IAutomaticLinkedBuilder<IDomainSecurityAuthorizationBuilder<E>, IDomainSecurityBuilder<E>, Object>{

    IDomainSecurityAuthorizationBuilder<E> interfasse(IClass<? extends IEndpoint> interfaceClass) throws ApiException;

}

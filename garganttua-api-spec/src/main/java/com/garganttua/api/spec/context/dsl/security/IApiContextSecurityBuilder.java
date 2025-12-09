package com.garganttua.api.spec.context.dsl.security;

import java.util.Optional;

import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.security.IApiSecurityContext;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.supply.IObjectSupplier;
import com.garganttua.core.supply.dsl.IObjectSupplierBuilder;

public interface IApiContextSecurityBuilder
        extends IAutomaticLinkedBuilder<IApiContextSecurityBuilder, IApiContextBuilder, IApiSecurityContext> {

    IAuthenticationBuilder authentication(IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier) throws DslException;

    IAuthenticationBuilder authentication(Class<?> authenticationClass) throws DslException;

    IAuthorizationProtocolBuilder authorizationProtocol(IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier) throws DslException;

    IAuthorizationProtocolBuilder authorizationProtocol(Class<?> authorizationProtocolClass) throws DslException;

    Optional<IAuthenticationBuilder> isAuthenticationAvailable(Class<?> authenticationClass);

    IApiContextSecurityBuilder disable(boolean b);

}

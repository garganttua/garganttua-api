package com.garganttua.api.spec.context.dsl.security;

import java.util.Optional;

import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.security.IApiSecurityContext;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;
import com.garganttua.core.dsl.IPackageableBuilder;

public interface IApiContextSecurityBuilder
        extends IAutomaticLinkedBuilder<IApiContextSecurityBuilder, IApiContextBuilder, IApiSecurityContext>, IPackageableBuilder<IApiContextSecurityBuilder, IApiSecurityContext> {

    IAuthenticationBuilder authentication(ISupplierBuilder<?, ? extends ISupplier<?>> supplier) throws DslException;

    IAuthenticationBuilder authentication(Class<?> authenticationClass) throws DslException;

    IAuthorizationProtocolBuilder authorizationProtocol(ISupplierBuilder<?, ? extends ISupplier<?>> supplier) throws DslException;

    IAuthorizationProtocolBuilder authorizationProtocol(Class<?> authorizationProtocolClass) throws DslException;

    Optional<IAuthenticationBuilder> isAuthenticationAvailable(Class<?> authenticationClass);

    IApiContextSecurityBuilder disable(boolean b);

}

package com.garganttua.api.spec.context.dsl.security;

import java.util.Optional;

import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.security.IApiSecurityContext;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;
import com.garganttua.core.dsl.IPackageableBuilder;

public interface IApiContextSecurityBuilder
        extends IAutomaticLinkedBuilder<IApiContextSecurityBuilder, IApiContextBuilder, IApiSecurityContext>, IPackageableBuilder<IApiContextSecurityBuilder, IApiSecurityContext> {

    IAuthenticationBuilder authentication(ISupplierBuilder<?, ? extends ISupplier<?>> supplier) throws ApiException;

    IAuthenticationBuilder authentication(Class<?> authenticationClass) throws ApiException;

    IAuthorizationProtocolBuilder authorizationProtocol(ISupplierBuilder<?, ? extends ISupplier<?>> supplier) throws ApiException;

    IAuthorizationProtocolBuilder authorizationProtocol(Class<?> authorizationProtocolClass) throws ApiException;

    Optional<IAuthenticationBuilder> isAuthenticationAvailable(Class<?> authenticationClass);

    IApiContextSecurityBuilder disable(boolean b);

}

package com.garganttua.api.spec.engine;

import java.util.Optional;

import com.garganttua.api.spec.CoreException;

public interface IContextSecurityBuilder
        extends IAutomaticLinkedBuilder<IApplicationSecurityContext, IApplicationContextBuilder, IContextSecurityBuilder> {

    IAuthenticationBuilder authentication(IObjectSupplierBuilder<?> supplier) throws CoreException;

    IAuthenticationBuilder authentication(Class<?> authenticationClass) throws CoreException;

    IAuthorizationProtocolBuilder authorizationProtocol(IObjectSupplierBuilder<?> supplier) throws CoreException;

    IAuthorizationProtocolBuilder authorizationProtocol(Class<?> authorizationProtocolClass) throws CoreException;

    Optional<IAuthenticationBuilder> isAuthenticationAvailable(Class<?> authenticationClass);

    IContextSecurityBuilder disable(boolean b);

}

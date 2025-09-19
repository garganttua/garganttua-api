package com.garganttua.api.spec.engine;

import com.garganttua.api.spec.CoreException;

public interface IContextSecurityBuilder
        extends IAutomaticLinkedBuilder<Object, IApplicationContextBuilder, IContextSecurityBuilder> {

    IAuthenticationBuilder authentication(Object authentication) throws CoreException;

    IAuthenticationBuilder authentication(IObjectSupplier<?> supplier) throws CoreException;

    IAuthorizationProtocolBuilder authorizationProtocol(Object protocol) throws CoreException;

    IAuthorizationProtocolBuilder authorizationProtocol(IObjectSupplier<?> supplier) throws CoreException;

}

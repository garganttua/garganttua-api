package com.garganttua.api.spec.engine;

import java.lang.reflect.Method;

import com.garganttua.api.spec.CoreException;
import com.garganttua.reflection.GGObjectAddress;

public interface IAuthorizationProtocolBuilder
        extends IAutomaticLinkedBuilder<IAuthorizationProtocolContext, IContextSecurityBuilder, IAuthorizationProtocolBuilder> {

    IAuthorizationProtocolBuilder getAuthorization(String methodName) throws CoreException;

    IAuthorizationProtocolBuilder getAuthorization(Method method) throws CoreException;

    IAuthorizationProtocolBuilder getAuthorization(GGObjectAddress methodAddress) throws CoreException;

    IAuthorizationProtocolBuilder setAuthorization(String methodName) throws CoreException;

    IAuthorizationProtocolBuilder setAuthorization(Method method) throws CoreException;

    IAuthorizationProtocolBuilder setAuthorization(GGObjectAddress methodAddress) throws CoreException;
}

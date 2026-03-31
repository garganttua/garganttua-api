package com.garganttua.api.spec.context.dsl.security;

import com.garganttua.core.reflection.IMethod;

import com.garganttua.api.spec.security.context.IAuthorizationProtocolContext;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;

public interface IAuthorizationProtocolBuilder
        extends IAutomaticLinkedBuilder<IAuthorizationProtocolBuilder, IApiSecurityBuilder, IAuthorizationProtocolContext> {

    IAuthorizationProtocolBuilder getAuthorization(String methodName) throws ApiException;

    IAuthorizationProtocolBuilder getAuthorization(IMethod method) throws ApiException;

    IAuthorizationProtocolBuilder getAuthorization(ObjectAddress methodAddress) throws ApiException;

    IAuthorizationProtocolBuilder setAuthorization(String methodName) throws ApiException;

    IAuthorizationProtocolBuilder setAuthorization(IMethod method) throws ApiException;

    IAuthorizationProtocolBuilder setAuthorization(ObjectAddress methodAddress) throws ApiException;
}

package com.garganttua.api.spec.context.dsl.security;

import java.lang.reflect.Method;

import com.garganttua.api.spec.security.authorization.IAuthorizationProtocol;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;

public interface IAuthorizationProtocolBuilder
        extends IAutomaticLinkedBuilder<IAuthorizationProtocolBuilder, IApiContextSecurityBuilder, IAuthorizationProtocol> {

    IAuthorizationProtocolBuilder getAuthorization(String methodName) throws DslException;

    IAuthorizationProtocolBuilder getAuthorization(Method method) throws DslException;

    IAuthorizationProtocolBuilder getAuthorization(ObjectAddress methodAddress) throws DslException;

    IAuthorizationProtocolBuilder setAuthorization(String methodName) throws DslException;

    IAuthorizationProtocolBuilder setAuthorization(Method method) throws DslException;

    IAuthorizationProtocolBuilder setAuthorization(ObjectAddress methodAddress) throws DslException;
}

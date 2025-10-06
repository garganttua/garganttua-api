package com.garganttua.api.spec.engine;

import java.lang.reflect.Method;

import com.garganttua.api.spec.CoreException;
import com.garganttua.reflection.GGObjectAddress;

public interface ISignableAuthorizationBuilder extends IAutomaticLinkedBuilder<Object, IAuthorizationBuilder, ISignableAuthorizationBuilder> {

    ISignableAuthorizationBuilder setSignature(String string) throws CoreException;

    ISignableAuthorizationBuilder setSignature(Method method) throws CoreException;

    ISignableAuthorizationBuilder setSignature(GGObjectAddress fieldAddress) throws CoreException;

    ISignableAuthorizationBuilder getSignature(String string) throws CoreException;

    ISignableAuthorizationBuilder getSignature(Method method) throws CoreException;

    ISignableAuthorizationBuilder getSignature(GGObjectAddress fieldAddress) throws CoreException;

    ISignableAuthorizationBuilder getDataToSign(String string) throws CoreException;

    ISignableAuthorizationBuilder getDataToSign(Method method) throws CoreException;

    ISignableAuthorizationBuilder getDataToSign(GGObjectAddress fieldAddress) throws CoreException;

}

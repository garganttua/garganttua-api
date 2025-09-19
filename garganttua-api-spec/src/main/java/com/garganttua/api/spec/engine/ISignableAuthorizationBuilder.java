package com.garganttua.api.spec.engine;

import java.lang.reflect.Method;

import com.garganttua.api.spec.CoreException;
import com.garganttua.reflection.GGObjectAddress;

public interface ISignableAuthorizationBuilder extends IAutomaticLinkedBuilder<Object, IAuthorizationBuilder, ISignableAuthorizationBuilder> {

    ISignableAuthorizationBuilder sign(String string) throws CoreException;

    ISignableAuthorizationBuilder sign(Method method) throws CoreException;

    ISignableAuthorizationBuilder sign(GGObjectAddress fieldAddress) throws CoreException;

}

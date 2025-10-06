package com.garganttua.api.spec.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.security.IDomainSecurityBuilder;
import com.garganttua.reflection.GGObjectAddress;

public interface IAuthorizationBuilder
        extends IAutomaticLinkedBuilder<IAuthorizationContext, IDomainSecurityBuilder, IAuthorizationBuilder> {

    IAuthorizationBuilder type(Field field) throws CoreException;

    IAuthorizationBuilder type(String fieldName) throws CoreException;

    IAuthorizationBuilder type(GGObjectAddress fieldAddress) throws CoreException;

    IAuthorizationBuilder authorities(String fieldName) throws CoreException;

    IAuthorizationBuilder authorities(Field field) throws CoreException;

    IAuthorizationBuilder authorities(GGObjectAddress fieldAddress) throws CoreException;

    IAuthorizationBuilder expirable(GGObjectAddress fieldAddress) throws CoreException;

    IAuthorizationBuilder expirable(Field field) throws CoreException;

    IAuthorizationBuilder expirable(String fieldName) throws CoreException;

    IAuthorizationBuilder revokable(String fieldName) throws CoreException;

    IAuthorizationBuilder revokable(Field field) throws CoreException;

    IAuthorizationBuilder revokable(GGObjectAddress fieldAddress) throws CoreException;

    IAuthorizationBuilder encode(Method method) throws CoreException;

    IAuthorizationBuilder encode(String methodName) throws CoreException;

    IAuthorizationBuilder encode(GGObjectAddress methodAddress) throws CoreException;

    IAuthorizationBuilder decode(Method method) throws CoreException;

    IAuthorizationBuilder decode(String methodName) throws CoreException;

    IAuthorizationBuilder decode(GGObjectAddress methodAddress) throws CoreException;

    IAuthorizationBuilder storable(boolean b);

    IRefreshableAuthorizationBuilder refreshable();

    ISignableAuthorizationBuilder signable();

    Boolean isStorable();

    Boolean isRefreshable();

}

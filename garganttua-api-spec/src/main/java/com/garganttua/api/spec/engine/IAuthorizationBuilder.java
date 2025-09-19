package com.garganttua.api.spec.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.garganttua.api.spec.CoreException;
import com.garganttua.reflection.GGObjectAddress;

public interface IAuthorizationBuilder extends IAutomaticLinkedBuilder<Object, IDomainBuilder, IAuthorizationBuilder> {

    IAuthorizationBuilder type(Field field) throws CoreException;

    IAuthorizationBuilder type(String fieldName) throws CoreException;

    IAuthorizationBuilder type(GGObjectAddress fieldAddress) throws CoreException;

    IAuthorizationBuilder authorities(String fieldName) throws CoreException;

    IAuthorizationBuilder authorities(Field field) throws CoreException;

    IAuthorizationBuilder authorities(GGObjectAddress fieldAddress) throws CoreException;

    IAuthorizationBuilder creation(String fieldName) throws CoreException;

    IAuthorizationBuilder creation(Field field) throws CoreException;

    IAuthorizationBuilder creation(GGObjectAddress fieldAddress) throws CoreException;

    IAuthorizationBuilder expiration(GGObjectAddress fieldAddress) throws CoreException;

    IAuthorizationBuilder expiration(Field field) throws CoreException;

    IAuthorizationBuilder expiration(String fieldName) throws CoreException;

    IAuthorizationBuilder revoked(String fieldName) throws CoreException;

    IAuthorizationBuilder revoked(Field field) throws CoreException;

    IAuthorizationBuilder revoked(GGObjectAddress fieldAddress) throws CoreException;

    IAuthorizationBuilder toByteArray(Method method) throws CoreException;

    IAuthorizationBuilder toByteArray(String methodName) throws CoreException;

    IAuthorizationBuilder toByteArray(GGObjectAddress methodAddress) throws CoreException;

    IAuthorizationBuilder fromByteArray(Method method) throws CoreException;

    IAuthorizationBuilder fromByteArray(String methodName) throws CoreException;

    IAuthorizationBuilder fromByteArray(GGObjectAddress methodAddress) throws CoreException;

    IAuthorizationBuilder validate(String methodName) throws CoreException;

    IAuthorizationBuilder validate(Method method) throws CoreException;

    IAuthorizationBuilder validate(GGObjectAddress methodAddress) throws CoreException;

    IAuthorizationBuilder validateAgainst(String methodName) throws CoreException;

    IAuthorizationBuilder validateAgainst(Method method) throws CoreException;

    IAuthorizationBuilder validateAgainst(GGObjectAddress methodAddress) throws CoreException;

    IAuthorizationBuilder storable(boolean b);

    IRefreshableAuthorizationBuilder refreshable();

    ISignableAuthorizationBuilder signable(IDomainBuilder key);

}

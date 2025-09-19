package com.garganttua.api.spec.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.garganttua.api.spec.CoreException;
import com.garganttua.reflection.GGObjectAddress;

public interface IRefreshableAuthorizationBuilder extends IAutomaticLinkedBuilder<Object, IAuthorizationBuilder, IRefreshableAuthorizationBuilder> {

    IRefreshableAuthorizationBuilder expiration(GGObjectAddress fieldAddress) throws CoreException;

    IRefreshableAuthorizationBuilder expiration(Field field) throws CoreException;

    IRefreshableAuthorizationBuilder expiration(String fieldName) throws CoreException;

    IRefreshableAuthorizationBuilder revoked(String fieldName) throws CoreException;

    IRefreshableAuthorizationBuilder revoked(Field field) throws CoreException;

    IRefreshableAuthorizationBuilder revoked(GGObjectAddress fieldAddress) throws CoreException;

    IRefreshableAuthorizationBuilder toByteArray(Method method) throws CoreException;

    IRefreshableAuthorizationBuilder toByteArray(String methodName) throws CoreException;

    IRefreshableAuthorizationBuilder toByteArray(GGObjectAddress fieldAddress) throws CoreException;

    IRefreshableAuthorizationBuilder validate(String methodName) throws CoreException;

    IRefreshableAuthorizationBuilder validate(Method method) throws CoreException;

    IRefreshableAuthorizationBuilder validate(GGObjectAddress fieldAddress) throws CoreException;

    IRefreshableAuthorizationBuilder validateAgainst(String methodName) throws CoreException;

    IRefreshableAuthorizationBuilder validateAgainst(Method method) throws CoreException;

    IRefreshableAuthorizationBuilder validateAgainst(GGObjectAddress fieldAddress) throws CoreException;

}

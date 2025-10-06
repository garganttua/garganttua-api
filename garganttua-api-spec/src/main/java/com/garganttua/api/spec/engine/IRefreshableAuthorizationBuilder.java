package com.garganttua.api.spec.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.garganttua.api.spec.CoreException;
import com.garganttua.reflection.GGObjectAddress;

public interface IRefreshableAuthorizationBuilder
        extends IAutomaticLinkedBuilder<Object, IAuthorizationBuilder, IRefreshableAuthorizationBuilder> {

    IRefreshableAuthorizationBuilder expirable(GGObjectAddress fieldAddress) throws CoreException;

    IRefreshableAuthorizationBuilder expirable(Field field) throws CoreException;

    IRefreshableAuthorizationBuilder expirable(String fieldName) throws CoreException;

    IRefreshableAuthorizationBuilder revokable(String fieldName) throws CoreException;

    IRefreshableAuthorizationBuilder revokable(Field field) throws CoreException;

    IRefreshableAuthorizationBuilder revokable(GGObjectAddress fieldAddress) throws CoreException;

    IRefreshableAuthorizationBuilder encode(Method method) throws CoreException;

    IRefreshableAuthorizationBuilder encode(String methodName) throws CoreException;

    IRefreshableAuthorizationBuilder encode(GGObjectAddress fieldAddress) throws CoreException;

    IRefreshableAuthorizationBuilder decode(Method method) throws CoreException;

    IRefreshableAuthorizationBuilder decode(String methodName) throws CoreException;

    IRefreshableAuthorizationBuilder decode(GGObjectAddress fieldAddress) throws CoreException;

    /*
     * IRefreshableAuthorizationBuilder validateAgainst(String methodName) throws
     * CoreException;
     * 
     * IRefreshableAuthorizationBuilder validateAgainst(Method method) throws
     * CoreException;
     * 
     * IRefreshableAuthorizationBuilder validateAgainst(GGObjectAddress
     * fieldAddress) throws CoreException;
     */

}

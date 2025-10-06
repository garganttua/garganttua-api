package com.garganttua.api.spec.engine;

import java.lang.reflect.Method;

import com.garganttua.api.spec.CoreException;
import com.garganttua.reflection.GGObjectAddress;

public interface IMethodBinderBuilder<Returned extends IMethodBinderBuilder<Returned, Up>, Up>
                extends ILinkedBuilder<IMethodBinder, Up> {

        Returned method() throws CoreException;

        Returned method(Method method) throws CoreException;

        Returned method(GGObjectAddress methodAddress) throws CoreException;

        Returned method(String methodName) throws CoreException;

        Returned method(Method method,
                        Class<?> returnType, Class<?>... parameterTypes) throws CoreException;

        Returned method(GGObjectAddress methodAddress,
                        Class<?> returnType, Class<?>... parameterTypes) throws CoreException;

        Returned method(String methodName,
                        Class<?> returnType, Class<?>... parameterTypes) throws CoreException;

        Returned withParam(int i, Object parameter) throws CoreException;

        Returned withParam(int i, IObjectSupplierBuilder<?> supplier) throws CoreException;

        Returned withParam(String paramName, Object parameter) throws CoreException;

        Returned withParam(String paramName, IObjectSupplierBuilder<?> supplier) throws CoreException;

        Returned withParam(Object parameter) throws CoreException;

        Returned withParam(IObjectSupplierBuilder<?> supplier) throws CoreException;

        Returned withParam(int i, Object parameter, boolean acceptNullable) throws CoreException;

        Returned withParam(int i, IObjectSupplierBuilder<?> supplier, boolean acceptNullable) throws CoreException;

        Returned withParam(String paramName, Object parameter, boolean acceptNullable) throws CoreException;

        Returned withParam(String paramName, IObjectSupplierBuilder<?> supplier, boolean acceptNullable) throws CoreException;

        Returned withParam(Object parameter, boolean acceptNullable) throws CoreException;

        Returned withParam(IObjectSupplierBuilder<?> supplier, boolean acceptNullable) throws CoreException;

}

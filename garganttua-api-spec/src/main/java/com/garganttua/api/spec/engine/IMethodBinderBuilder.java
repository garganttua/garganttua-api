package com.garganttua.api.spec.engine;

import java.lang.reflect.Method;

import com.garganttua.api.spec.CoreException;
import com.garganttua.reflection.GGObjectAddress;

public interface IMethodBinderBuilder <T extends IMethodBinderBuilder<T>> {

    T method(Method method) throws CoreException;

    T method(GGObjectAddress method) throws CoreException;

    T method(String method) throws CoreException;

    T withParam(int i, Object service) throws CoreException;

    T withParam(Object service) throws CoreException;

    T withParam(IObjectSupplier<?> service) throws CoreException;

    T withParam(int i, IObjectSupplier<?> service) throws CoreException;

}

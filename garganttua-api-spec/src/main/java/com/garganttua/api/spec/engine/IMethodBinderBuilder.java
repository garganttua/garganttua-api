package com.garganttua.api.spec.engine;

import java.lang.reflect.Method;

import com.garganttua.api.spec.CoreException;
import com.garganttua.reflection.GGObjectAddress;

public interface IMethodBinderBuilder <Returned extends IMethodBinderBuilder<Returned, Built, Up>, Built, Up> extends IBuilder<Built, Up> {

    Returned method(Method method) throws CoreException;

    Returned method(GGObjectAddress method) throws CoreException;

    Returned method(String method) throws CoreException;

    Returned withParam(int i, Object parameter) throws CoreException;

   /*  Returned withParam(Object service) throws CoreException; */

    /* Returned withParam(IObjectSupplier<?> service) throws CoreException; */

    Returned withParam(int i, IObjectSupplier<?> supplier) throws CoreException;

}

package com.garganttua.api.spec.engine;

import com.garganttua.api.spec.CoreException;

public interface IObjectSupplier <T> {

    T getObject() throws CoreException;

    Class<T> getObjectClass() throws CoreException;

}

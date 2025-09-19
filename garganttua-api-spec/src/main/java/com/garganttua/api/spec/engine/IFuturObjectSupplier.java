package com.garganttua.api.spec.engine;

import com.garganttua.api.spec.CoreException;

public interface IFuturObjectSupplier<T> extends IObjectSupplier<T> {

    void setObject(T object) throws CoreException;

}

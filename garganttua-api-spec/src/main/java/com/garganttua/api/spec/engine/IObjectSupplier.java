package com.garganttua.api.spec.engine;

import java.util.Optional;

import com.garganttua.api.spec.CoreException;

public interface IObjectSupplier<ObjectType> {

    Optional<ObjectType> getObject(IApplicationContext aContext, IExecutionContext eContext) throws CoreException;

    Optional<ObjectType> getObject(IExecutionContext context) throws CoreException;

    Optional<ObjectType> getObject(IApplicationContext context) throws CoreException;

    Optional<ObjectType> getObject() throws CoreException;

    Class<ObjectType> getObjectClass();

}

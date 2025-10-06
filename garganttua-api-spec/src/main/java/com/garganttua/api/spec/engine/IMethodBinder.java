package com.garganttua.api.spec.engine;

import com.garganttua.api.spec.CoreException;

public interface IMethodBinder {

    <Returned> Returned execute() throws CoreException;

    <Returned> Returned execute(IExecutionContext executionContext, IApplicationContext applicationContext)
            throws CoreException;

}

package com.garganttua.api.core.runtime;

import java.util.concurrent.ExecutionException;

import com.garganttua.api.spec.engine.IExecutionContext;

public interface IRuntime {

    IExecutionContext execute() throws ExecutionException;

}

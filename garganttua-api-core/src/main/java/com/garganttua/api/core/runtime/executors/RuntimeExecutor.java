package com.garganttua.api.core.runtime.executors;

import org.javatuples.Pair;

import com.garganttua.api.core.context.application.ApplicationContext;
import com.garganttua.api.core.context.execution.ExecutionContext;
import com.garganttua.core.CoreException;
import com.garganttua.api.spec.context.IApplicationContext;
import com.garganttua.api.spec.context.IExecutionContext;
import com.garganttua.executor.chain.GGExecutorException;
import com.garganttua.executor.chain.IGGExecutor;
import com.garganttua.executor.chain.IGGExecutorChain;

public abstract class RuntimeExecutor
        implements IGGExecutor<Pair<IApplicationContext, IExecutionContext>> {

    @Override
    public void execute(Pair<IApplicationContext, IExecutionContext> ctxt,
            IGGExecutorChain<Pair<IApplicationContext, IExecutionContext>> next) throws GGExecutorException {
        ApplicationContext aCtxt = (ApplicationContext) ctxt.getValue0();
        ExecutionContext eCtxt = (ExecutionContext) ctxt.getValue1();

        try {
            this.execute(aCtxt, eCtxt);
        } catch (CoreException e) {
            throw new GGExecutorException(e);
        }

        next.execute(ctxt);
    }

    protected abstract void execute(IApplicationContext aCtxt, IExecutionContext eCtxt) throws CoreException;

}

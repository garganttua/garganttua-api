package com.garganttua.api.core.runtime.executors;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.core.context.application.ApplicationContext;
import com.garganttua.api.core.context.execution.ExecutionContext;
import com.garganttua.api.spec.context.IApplicationContext;
import com.garganttua.api.spec.context.IExecutionContext;
import com.garganttua.core.dsl.binder.IMethodBinderBuilder;
import com.garganttua.executor.chain.GGExecutorException;
import com.garganttua.executor.chain.IGGExecutor;
import com.garganttua.executor.chain.IGGExecutorChain;

public class SetAfterGetMethodsInExecutionContextVariables
        implements IGGExecutor<Pair<IApplicationContext, IExecutionContext>> {

    @Override
    public void execute(Pair<IApplicationContext, IExecutionContext> ctxt,
            IGGExecutorChain<Pair<IApplicationContext, IExecutionContext>> next) throws GGExecutorException {
        ApplicationContext aCtxt = (ApplicationContext) ctxt.getValue0();
        ExecutionContext eCtxt = (ExecutionContext) ctxt.getValue1();

        List<IMethodBinderBuilder<?, ?>> methods = aCtxt.getDomainContext(eCtxt.getDomainName()).get()
                .getAfterGetMethods();
        eCtxt.setAfterOperationMethods(methods);

        next.execute(ctxt);
    }

}

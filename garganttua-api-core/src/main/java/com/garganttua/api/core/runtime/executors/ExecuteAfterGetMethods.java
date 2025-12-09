package com.garganttua.api.core.runtime.executors;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.core.context.application.ApplicationContext;
import com.garganttua.api.core.context.execution.ExecutionContext;
import com.garganttua.core.CoreException;
import com.garganttua.api.spec.context.IApplicationContext;
import com.garganttua.api.spec.context.IExecutionContext;
import com.garganttua.core.dsl.binder.IMethodBinderBuilder;
import com.garganttua.executor.chain.GGExecutorException;
import com.garganttua.executor.chain.IGGExecutor;
import com.garganttua.executor.chain.IGGExecutorChain;

public class ExecuteAfterGetMethods implements IGGExecutor<Pair<IApplicationContext, IExecutionContext>> {

    @Override
    public void execute(Pair<IApplicationContext, IExecutionContext> context,
            IGGExecutorChain<Pair<IApplicationContext, IExecutionContext>> next) throws GGExecutorException {
        ApplicationContext aCtxt = (ApplicationContext) context.getValue0();
        ExecutionContext eCtxt = (ExecutionContext) context.getValue1();

        List<IMethodBinderBuilder<?, ?>> methods = eCtxt.getAfterOperationMethods();

        methods.forEach(method -> {
            try {
                method.build().execute(eCtxt, aCtxt);
            } catch (CoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });

        next.execute(context);
    }

}

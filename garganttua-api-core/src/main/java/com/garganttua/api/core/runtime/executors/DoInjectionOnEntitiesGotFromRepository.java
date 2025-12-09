package com.garganttua.api.core.runtime.executors;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.garganttua.api.core.context.execution.ExecutionVariable;
import com.garganttua.core.CoreException;
import com.garganttua.api.spec.context.IApplicationContext;
import com.garganttua.api.spec.context.IExecutionContext;
import com.garganttua.core.dsl.binder.IMethodBinderBuilder;

public class DoInjectionOnEntitiesGotFromRepository extends RuntimeExecutor {

    @Override
    protected void execute(IApplicationContext aCtxt, IExecutionContext eCtxt) throws CoreException {

        Optional<?> repoReturnedOpt = eCtxt.getExecutionVariable(ExecutionVariable.REPOSITORY_RETURN.toString());
        List<IMethodBinderBuilder<?, ?>> methods = eCtxt.getAfterOperationMethods();

        if( methods == null || methods.size() == 0 )
            return;

        if (repoReturnedOpt.isPresent()) {
            Object temp = repoReturnedOpt.get();
            if (Collection.class.isAssignableFrom(temp.getClass())) {
                List<?> entities = (List<?>) temp;
                for (Object entity : entities) {
                    aCtxt.doInjection(entity);
                }
            } else {
                aCtxt.doInjection(temp);
            }
        }
    }

}

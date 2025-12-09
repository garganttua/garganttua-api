package com.garganttua.api.core.runtime.executors;

import org.javatuples.Pair;

import static com.garganttua.api.core.context.application.ApplicationContext.Suppliers.repository;

import java.util.Optional;

import com.garganttua.api.core.context.application.ApplicationContext;
import com.garganttua.api.core.context.execution.ExecutionContext;
import com.garganttua.api.core.context.execution.ExecutionVariable;
import com.garganttua.core.CoreException;
import com.garganttua.api.spec.context.IApplicationContext;
import com.garganttua.api.spec.context.IExecutionContext;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.core.supply.IObjectSupplier;
import com.garganttua.executor.chain.GGExecutorException;
import com.garganttua.executor.chain.IGGExecutor;
import com.garganttua.executor.chain.IGGExecutorChain;

public class SetRepositoryInExecutionContextVariables implements IGGExecutor<Pair<IApplicationContext, IExecutionContext>> {

    @Override
    public void execute(Pair<IApplicationContext, IExecutionContext> context,
            IGGExecutorChain<Pair<IApplicationContext, IExecutionContext>> next) throws GGExecutorException {

        ApplicationContext actxt = (ApplicationContext) context.getValue0();
        ExecutionContext eCtxt = (ExecutionContext) context.getValue1();

        try {
            IObjectSupplier<IRepository> repositorySupplier = repository(eCtxt.getDomainName());

            Optional<IRepository> repository = repositorySupplier.getObject(actxt, eCtxt);
            if ( repository.isPresent() ) {
                eCtxt.setExecutionVariable(ExecutionVariable.REPOSITORY.toString(), repository.get());
            } else {
                throw new GGExecutorException("Repository not found for domain "+eCtxt.getDomainName());
            }

        } catch (CoreException e) {
            throw new GGExecutorException(e);
        }

        next.execute(context);
    }

}

package com.garganttua.api.core.runtime;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.javatuples.Pair;

import com.garganttua.api.core.context.application.ApplicationContext;
import com.garganttua.api.core.context.execution.ExecutionContext;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.BusinessOperation;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IExecutionContext;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.api.spec.engine.StandardParameter;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.executor.chain.GGExecutorChain;
import com.garganttua.executor.chain.GGExecutorException;
import com.garganttua.executor.chain.IGGExecutorChain;

public class Runtime implements IRuntime {

    public IApplicationContext applicationContext;
    private IExecutionContext executionContext;
    private IGGExecutorChain<Pair<IApplicationContext, IExecutionContext>> chain;

    public Runtime(IApplicationContext applicationContext, IExecutionContext executionContext,
            IGGExecutorChain<Pair<IApplicationContext, IExecutionContext>> chain) {
        this.applicationContext = applicationContext;
        this.executionContext = executionContext;
        this.chain = chain;
    }

    public static IRuntime createRuntimeWithoutSecurity(IApplicationContext applicationContext,
            IExecutionContext executionContext) {
        return null;

    }

    public static IRuntime createRuntime(IApplicationContext applicationContext, IExecutionContext executionContext)
            throws CoreException {
        IGGExecutorChain<Pair<IApplicationContext, IExecutionContext>> chain = new GGExecutorChain<>();

        Runtime.createCommonRuntime(chain, executionContext);

        if (executionContext.isAuthentication())
            Runtime.createAuthenticateRuntime(chain, executionContext);

        if (executionContext.isStandard())
            Runtime.createStandardRuntime(chain, executionContext);

        if (executionContext.isUseCase())
            Runtime.createUseCaseRuntime(chain, executionContext);

        Runtime.createFinalRuntime(chain, executionContext);

        return new Runtime(applicationContext, executionContext, chain);
    }

    private static void createFinalRuntime(IGGExecutorChain<Pair<IApplicationContext, IExecutionContext>> chain,
            IExecutionContext executionContext) {

        // Set response
        chain.addExecutor(
                (ctxt, next) -> {
                    ExecutionContext eCtxt = (ExecutionContext) ctxt.getValue1();

                    Optional<?> repositoryReturn = eCtxt
                            .getExecutionVariable(ExecutionVariable.REPOSITORY_RETURN.toString());
                    if (repositoryReturn.isPresent())
                        eCtxt.setResponse(repositoryReturn.get());

                    next.execute(ctxt);
                });

        // create output with body, return code

        // finalise event

        // send event
    }

    private static void createAuthenticateRuntime(
            IGGExecutorChain<Pair<IApplicationContext, IExecutionContext>> chain, IExecutionContext executionContext) {

    }

    private static void createCommonRuntime(IGGExecutorChain<Pair<IApplicationContext, IExecutionContext>> chain,
            IExecutionContext executionContext) {

    }

    private static void createUseCaseRuntime(
            IGGExecutorChain<Pair<IApplicationContext, IExecutionContext>> chain, IExecutionContext executionContext) {

    }

    private static void createStandardRuntime(
            IGGExecutorChain<Pair<IApplicationContext, IExecutionContext>> chain, IExecutionContext executionContext)
            throws CoreException {

        chain.addExecutor(new SetRepositoryInExecutionContextVariables());

        if (executionContext.getBusinessOperation() == BusinessOperation.readAll) {
            chain.addExecutor(new SetAfterGetMethodsInExecutionContextVariables());
            chain.addExecutor(new ReadAllRepositoryCall());
            chain.addExecutor(new ExecuteAfterGetMethods());
        }
    }

    @Override
    public IExecutionContext execute() throws ExecutionException {
        try {
            this.chain.execute(new Pair<>(this.applicationContext, this.executionContext));
        } catch (GGExecutorException e) {
            throw new ExecutionException(e);
        }
        return this.executionContext;
    }
}

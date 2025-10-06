package com.garganttua.api.core.runtime;

import java.util.List;
import java.util.Optional;

import org.javatuples.Pair;

import com.garganttua.api.core.context.execution.ExecutionContext;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IExecutionContext;
import com.garganttua.api.spec.engine.StandardParameter;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.executor.chain.GGExecutorException;
import com.garganttua.executor.chain.IGGExecutor;
import com.garganttua.executor.chain.IGGExecutorChain;

public class ReadAllRepositoryCall implements IGGExecutor<Pair<IApplicationContext, IExecutionContext>> {

    @Override
    public void execute(Pair<IApplicationContext, IExecutionContext> ctxt,
            IGGExecutorChain<Pair<IApplicationContext, IExecutionContext>> next) throws GGExecutorException {

        ExecutionContext eCtxt = (ExecutionContext) ctxt.getValue1();

        Optional<IFilter> filter = eCtxt.getExecutionVariable(StandardParameter.FILTER.toString(),
                IFilter.class);
        Optional<IPageable> page = eCtxt.getExecutionVariable(StandardParameter.PAGE.toString(),
                IPageable.class);
        Optional<ISort> sort = eCtxt.getExecutionVariable(StandardParameter.SORT.toString(),
                ISort.class);

        Optional<IRepository> repository = eCtxt.getExecutionVariable(ExecutionVariable.REPOSITORY.toString(), IRepository.class);

        try {
            if (repository.isPresent()) {
                List<Object> entities = repository.get().getEntities(page,
                        filter, sort);
                eCtxt.setExecutionVariable(ExecutionVariable.REPOSITORY_RETURN.toString(), entities);
            }
        } catch (CoreException e) {
            throw new GGExecutorException(e);
        }
        next.execute(ctxt);

    }

}

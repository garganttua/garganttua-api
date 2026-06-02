package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.util.Optional;

import com.garganttua.api.commons.repository.IRepository;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

import com.garganttua.core.observability.Logger;

@SuppressWarnings("rawtypes")
public class RepositorySupplier implements IContextualSupplier<IRepository, IRuntimeContext> {
	private static final Logger log = Logger.getLogger(RepositorySupplier.class);


    private static final IClass<IRepository> SUPPLIED_CLASS = IClass.getClass(IRepository.class);
    private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<IRepository> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public IClass<IRuntimeContext> getOwnerContextType() {
        return CONTEXT_CLASS;
    }

    @Override
    public Optional<IRepository> supply(IRuntimeContext context, Object... otherContexts) throws SupplyException {
        log.trace("Entering RepositorySupplier.supply");

        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }

        Optional<?> requestOpt = context.getVariable("request", IClass.getClass(IOperationRequest.class));
        if (requestOpt.isEmpty()) {
            throw new SupplyException("Variable 'request' not found in runtime context");
        }
        IOperationRequest request = (IOperationRequest) requestOpt.get();

        Optional<IRepository> repositoryOpt = request.arg(IOperationRequest.REPOSITORY);
        log.debug("RepositorySupplier resolved repository (present={})", repositoryOpt.isPresent());
        return repositoryOpt;
    }

}

package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.util.Optional;

import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

import com.garganttua.core.observability.Logger;

@SuppressWarnings("rawtypes")
public class ApiSupplier implements IContextualSupplier<IApi, IRuntimeContext> {
	private static final Logger log = Logger.getLogger(ApiSupplier.class);


    private static final IClass<IApi> SUPPLIED_CLASS = IClass.getClass(IApi.class);
    private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<IApi> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public IClass<IRuntimeContext> getOwnerContextType() {
        return CONTEXT_CLASS;
    }

    @Override
    public Optional<IApi> supply(IRuntimeContext context, Object... otherContexts) throws SupplyException {
        log.trace("Entering ApiSupplier.supply");

        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }

        Optional<?> requestOpt = context.getVariable("request", IClass.getClass(IOperationRequest.class));
        if (requestOpt.isEmpty()) {
            throw new SupplyException("Variable 'request' not found in runtime context");
        }
        IOperationRequest request = (IOperationRequest) requestOpt.get();

        Optional<IApi> apiContextOpt = request.arg(IOperationRequest.API_CONTEXT);
        if (apiContextOpt.isEmpty()) {
            throw new SupplyException("API context not found in operation request");
        }

        log.debug("ApiSupplier resolved apiContext");
        return apiContextOpt;
    }

}

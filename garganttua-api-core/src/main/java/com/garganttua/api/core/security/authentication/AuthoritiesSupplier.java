package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

import com.garganttua.core.observability.Logger;

@SuppressWarnings("rawtypes")
public class AuthoritiesSupplier implements IContextualSupplier<List, IRuntimeContext> {
	private static final Logger log = Logger.getLogger(AuthoritiesSupplier.class);


    private static final IClass<List> SUPPLIED_CLASS = IClass.getClass(List.class);
    private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<List> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public IClass<IRuntimeContext> getOwnerContextType() {
        return CONTEXT_CLASS;
    }

    @Override
    public Optional<List> supply(IRuntimeContext context, Object... otherContexts) throws SupplyException {
        log.trace("Entering AuthoritiesSupplier.supply");

        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }

        Optional<?> requestOpt = context.getVariable("request", IClass.getClass(IOperationRequest.class));
        if (requestOpt.isEmpty()) {
            throw new SupplyException("Variable 'request' not found in runtime context");
        }
        IOperationRequest request = (IOperationRequest) requestOpt.get();

        List authorities = request.caller() != null ? request.caller().authorities() : null;
        log.debug("AuthoritiesSupplier resolved authorities (present={})", authorities != null);
        return Optional.ofNullable(authorities);
    }

}

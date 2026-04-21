package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("rawtypes")
public class AuthoritiesSupplier implements IContextualSupplier<List, IRuntimeContext> {

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
        log.atTrace().log("Entering AuthoritiesSupplier.supply");

        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }

        Optional<?> requestOpt = context.getVariable("request", IClass.getClass(IOperationRequest.class));
        if (requestOpt.isEmpty()) {
            throw new SupplyException("Variable 'request' not found in runtime context");
        }
        IOperationRequest request = (IOperationRequest) requestOpt.get();

        List authorities = request.caller() != null ? request.caller().authorities() : null;
        log.atDebug().log("AuthoritiesSupplier resolved authorities (present={})", authorities != null);
        return Optional.ofNullable(authorities);
    }

}

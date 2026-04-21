package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.UUID;

import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("rawtypes")
public class ExecutionUuidSupplier implements IContextualSupplier<UUID, IRuntimeContext> {

    private static final IClass<UUID> SUPPLIED_CLASS = IClass.getClass(UUID.class);
    private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<UUID> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public IClass<IRuntimeContext> getOwnerContextType() {
        return CONTEXT_CLASS;
    }

    @Override
    public Optional<UUID> supply(IRuntimeContext context, Object... otherContexts) throws SupplyException {
        log.atTrace().log("Entering ExecutionUuidSupplier.supply");

        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }

        Optional<?> requestOpt = context.getVariable("request", IClass.getClass(IOperationRequest.class));
        if (requestOpt.isEmpty()) {
            throw new SupplyException("Variable 'request' not found in runtime context");
        }
        IOperationRequest request = (IOperationRequest) requestOpt.get();

        UUID executionUuid = request.executionUuid();
        log.atDebug().log("ExecutionUuidSupplier resolved executionUuid={}", executionUuid);
        return Optional.ofNullable(executionUuid);
    }

}

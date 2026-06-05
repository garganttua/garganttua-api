package com.garganttua.api.core.security.authorization;

import java.lang.reflect.Type;
import java.util.Optional;

import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

import com.garganttua.core.observability.Logger;

/**
 * Supplies the in-flight {@link IOperationRequest} to a custom issuer method
 * parameter, read from the runtime variable {@code "request"} published by the
 * mint side. Lets an issuer scope the token or read transport data (headers,
 * args) — e.g. when delegating issuance to an external authorization server.
 */
public class RequestSupplier implements IContextualSupplier<IOperationRequest, IRuntimeContext> {
    private static final Logger log = Logger.getLogger(RequestSupplier.class);

    private static final IClass<IOperationRequest> SUPPLIED_CLASS = IClass.getClass(IOperationRequest.class);
    @SuppressWarnings("rawtypes")
    private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<IOperationRequest> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public IClass<IRuntimeContext> getOwnerContextType() {
        return CONTEXT_CLASS;
    }

    @Override
    public Optional<IOperationRequest> supply(IRuntimeContext context, Object... otherContexts) throws SupplyException {
        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }
        Optional<?> reqOpt = context.getVariable("request", SUPPLIED_CLASS);
        if (reqOpt.isEmpty()) {
            throw new SupplyException("Variable 'request' not found in runtime context");
        }
        log.trace("RequestSupplier resolved the operation request");
        return Optional.of((IOperationRequest) reqOpt.get());
    }

}

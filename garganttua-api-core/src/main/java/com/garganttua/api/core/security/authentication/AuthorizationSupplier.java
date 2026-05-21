package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.util.Optional;

import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

import lombok.extern.slf4j.Slf4j;

/**
 * Supplies the decoded authorization entity from {@code request.arg("authorization")}.
 *
 * <p>No interface contract is imposed on the entity — the framework treats it
 * as a plain {@link Object} and lets the DSL-declared fields drive signature,
 * expiration, and revocation checks. Authentication strategies pattern-match
 * on the runtime class to decide whether they handle this shape.
 */
@Slf4j
@SuppressWarnings("rawtypes")
public class AuthorizationSupplier implements IContextualSupplier<Object, IRuntimeContext> {

    private static final IClass<Object> SUPPLIED_CLASS = IClass.getClass(Object.class);
    private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<Object> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public IClass<IRuntimeContext> getOwnerContextType() {
        return CONTEXT_CLASS;
    }

    @Override
    public Optional<Object> supply(IRuntimeContext context, Object... otherContexts) throws SupplyException {
        log.atTrace().log("Entering AuthorizationSupplier.supply");

        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }

        Optional<?> requestOpt = context.getVariable("request", IClass.getClass(IOperationRequest.class));
        if (requestOpt.isEmpty()) {
            throw new SupplyException("Variable 'request' not found in runtime context");
        }
        IOperationRequest request = (IOperationRequest) requestOpt.get();

        Optional<Object> authorizationOpt = request.arg(IOperationRequest.AUTHORIZATION);
        log.atDebug().log("AuthorizationSupplier resolved authorization (present={})", authorizationOpt.isPresent());
        return authorizationOpt;
    }

}

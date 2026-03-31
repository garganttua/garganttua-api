package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.util.Optional;

import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

/**
 * Supplies the principal (authenticator entity found by login) for the authenticate method.
 * Reads the "principal" variable from the operation request, set by AUTHENTICATE.gs
 * after findByLogin resolves the entity.
 */
@SuppressWarnings("rawtypes")
public class AuthenticatePrincipalSupplier implements IContextualSupplier<Object, IRuntimeContext> {

    private static final IClass<Object> SUPPLIED_CLASS = IClass.getClass(Object.class);
    private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

    @Override
    public Type getSuppliedType() { return SUPPLIED_CLASS.getType(); }

    @Override
    public IClass<Object> getSuppliedClass() { return SUPPLIED_CLASS; }

    @Override
    public IClass<IRuntimeContext> getOwnerContextType() { return CONTEXT_CLASS; }

    @Override
    public Optional<Object> supply(IRuntimeContext context, Object... otherContexts) throws SupplyException {
        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }

        Optional<?> requestOpt = context.getVariable("request", IClass.getClass(IOperationRequest.class));
        if (requestOpt.isEmpty()) {
            throw new SupplyException("Variable 'request' not found in runtime context");
        }
        IOperationRequest request = (IOperationRequest) requestOpt.get();

        // Read the principal set by AUTHENTICATE.gs via setRequestArg(@0, "principal", @principal)
        Optional<?> principal = request.arg("principal");
        if (principal.isEmpty()) {
            throw new SupplyException("Variable 'principal' not found in request — was findByLogin called?");
        }
        return Optional.of(principal.get());
    }
}

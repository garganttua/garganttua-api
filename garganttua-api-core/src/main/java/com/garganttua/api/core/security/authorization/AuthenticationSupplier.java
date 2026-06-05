package com.garganttua.api.core.security.authorization;

import java.lang.reflect.Type;
import java.util.Optional;

import com.garganttua.api.commons.security.authentication.IAuthentication;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

import com.garganttua.core.observability.Logger;

/**
 * Supplies the successful {@link IAuthentication} result to a custom issuer
 * method parameter. The mint side publishes it under the runtime variable
 * {@code "authentication"} (see {@code SecurityExpressions.issueAuthorization}),
 * symmetric to how the verify side publishes {@code "request"} /
 * {@code "domainContext"} for the authenticate method's suppliers.
 */
public class AuthenticationSupplier implements IContextualSupplier<IAuthentication, IRuntimeContext> {
    private static final Logger log = Logger.getLogger(AuthenticationSupplier.class);

    private static final IClass<IAuthentication> SUPPLIED_CLASS = IClass.getClass(IAuthentication.class);
    @SuppressWarnings("rawtypes")
    private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<IAuthentication> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public IClass<IRuntimeContext> getOwnerContextType() {
        return CONTEXT_CLASS;
    }

    @Override
    public Optional<IAuthentication> supply(IRuntimeContext context, Object... otherContexts) throws SupplyException {
        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }
        Optional<?> authOpt = context.getVariable("authentication", SUPPLIED_CLASS);
        if (authOpt.isEmpty()) {
            throw new SupplyException("Variable 'authentication' not found in runtime context");
        }
        log.trace("AuthenticationSupplier resolved the authentication result");
        return Optional.of((IAuthentication) authOpt.get());
    }

}

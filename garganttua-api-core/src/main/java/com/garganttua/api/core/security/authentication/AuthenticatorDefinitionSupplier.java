package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.util.Optional;

import com.garganttua.api.core.domain.Domain;
import com.garganttua.api.core.domain.DomainDefinition;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

import com.garganttua.core.observability.Logger;

@SuppressWarnings("rawtypes")
public class AuthenticatorDefinitionSupplier implements IContextualSupplier<IAuthenticatorDefinition, IRuntimeContext> {
	private static final Logger log = Logger.getLogger(AuthenticatorDefinitionSupplier.class);


    private static final IClass<IAuthenticatorDefinition> SUPPLIED_CLASS = IClass.getClass(IAuthenticatorDefinition.class);
    private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<IAuthenticatorDefinition> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public IClass<IRuntimeContext> getOwnerContextType() {
        return CONTEXT_CLASS;
    }

    @Override
    public Optional<IAuthenticatorDefinition> supply(IRuntimeContext context, Object... otherContexts) throws SupplyException {
        log.trace("Entering AuthenticatorDefinitionSupplier.supply");

        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }

        Optional<?> domainContextOpt = context.getVariable("domainContext", IClass.getClass(IDomain.class));
        if (domainContextOpt.isEmpty()) {
            throw new SupplyException("Variable 'domainContext' not found in runtime context");
        }

        IDomain domainContext = (IDomain) domainContextOpt.get();
        if (domainContext instanceof Domain<?> domCtx) {
            var domDef = (DomainDefinition<?>) domCtx.getDomainDefinition();
            var secDef = domDef.domainSecurityDefinition();
            if (secDef != null && secDef.authenticatorDefinition() != null) {
                return Optional.of(secDef.authenticatorDefinition());
            }
        }

        log.debug("No authenticator definition found in domain context");
        return Optional.empty();
    }

}

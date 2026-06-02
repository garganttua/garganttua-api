package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.util.Optional;

import com.garganttua.api.commons.context.IDomain;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

import com.garganttua.core.observability.Logger;

@SuppressWarnings("rawtypes")
public class DomainSupplier implements IContextualSupplier<IDomain, IRuntimeContext> {
	private static final Logger log = Logger.getLogger(DomainSupplier.class);


    private static final IClass<IDomain> SUPPLIED_CLASS = IClass.getClass(IDomain.class);
    private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<IDomain> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public IClass<IRuntimeContext> getOwnerContextType() {
        return CONTEXT_CLASS;
    }

    @Override
    public Optional<IDomain> supply(IRuntimeContext context, Object... otherContexts) throws SupplyException {
        log.trace("Entering DomainSupplier.supply");

        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }

        Optional<?> domainContextOpt = context.getVariable("domainContext", SUPPLIED_CLASS);
        if (domainContextOpt.isEmpty()) {
            throw new SupplyException("Variable 'domainContext' not found in runtime context");
        }

        IDomain domainContext = (IDomain) domainContextOpt.get();
        log.debug("DomainSupplier resolved domain={}", domainContext.getDomainName());
        return Optional.of(domainContext);
    }

}

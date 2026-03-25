package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.util.Optional;

import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("rawtypes")
public class DomainContextSupplier implements IContextualSupplier<IDomainContext, IRuntimeContext> {

    private static final IClass<IDomainContext> SUPPLIED_CLASS = IClass.getClass(IDomainContext.class);
    private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<IDomainContext> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public IClass<IRuntimeContext> getOwnerContextType() {
        return CONTEXT_CLASS;
    }

    @Override
    public Optional<IDomainContext> supply(IRuntimeContext context, Object... otherContexts) throws SupplyException {
        log.atTrace().log("Entering DomainContextSupplier.supply");

        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }

        Optional<?> domainContextOpt = context.getVariable("domainContext", SUPPLIED_CLASS);
        if (domainContextOpt.isEmpty()) {
            throw new SupplyException("Variable 'domainContext' not found in runtime context");
        }

        IDomainContext domainContext = (IDomainContext) domainContextOpt.get();
        log.atDebug().log("DomainContextSupplier resolved domain={}", domainContext.getDomainName());
        return Optional.of(domainContext);
    }

}

package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;

import com.garganttua.api.commons.context.IDomain;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import com.garganttua.core.observability.Logger;

@SuppressWarnings("rawtypes")
public class DomainSupplierBuilder implements ISupplierBuilder<IDomain, IContextualSupplier<IDomain, IRuntimeContext>> {
	private static final Logger log = Logger.getLogger(DomainSupplierBuilder.class);


    private static final IClass<IDomain> SUPPLIED_CLASS = IClass.getClass(IDomain.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<IDomain> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<IDomain, IRuntimeContext> build() throws DslException {
        log.debug("Building DomainSupplier");
        return new DomainSupplier();
    }

}

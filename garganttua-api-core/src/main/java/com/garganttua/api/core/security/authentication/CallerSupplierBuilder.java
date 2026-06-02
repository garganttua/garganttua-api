package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import com.garganttua.core.observability.Logger;

@SuppressWarnings("rawtypes")
public class CallerSupplierBuilder implements ISupplierBuilder<ICaller, IContextualSupplier<ICaller, IRuntimeContext>> {
	private static final Logger log = Logger.getLogger(CallerSupplierBuilder.class);


    private static final IClass<ICaller> SUPPLIED_CLASS = IClass.getClass(ICaller.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<ICaller> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<ICaller, IRuntimeContext> build() throws DslException {
        log.debug("Building CallerSupplier");
        return new CallerSupplier();
    }

}

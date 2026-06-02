package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;

import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import com.garganttua.core.observability.Logger;

@SuppressWarnings("rawtypes")
public class TenantSupplierBuilder implements ISupplierBuilder<String, IContextualSupplier<String, IRuntimeContext>> {
	private static final Logger log = Logger.getLogger(TenantSupplierBuilder.class);


    private static final IClass<String> SUPPLIED_CLASS = IClass.getClass(String.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<String> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<String, IRuntimeContext> build() throws DslException {
        log.debug("Building TenantSupplier");
        return new TenantSupplier();
    }

}

package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.util.UUID;

import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import com.garganttua.core.observability.Logger;

@SuppressWarnings("rawtypes")
public class ExecutionUuidSupplierBuilder implements ISupplierBuilder<UUID, IContextualSupplier<UUID, IRuntimeContext>> {
	private static final Logger log = Logger.getLogger(ExecutionUuidSupplierBuilder.class);


    private static final IClass<UUID> SUPPLIED_CLASS = IClass.getClass(UUID.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<UUID> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<UUID, IRuntimeContext> build() throws DslException {
        log.debug("Building ExecutionUuidSupplier");
        return new ExecutionUuidSupplier();
    }

}

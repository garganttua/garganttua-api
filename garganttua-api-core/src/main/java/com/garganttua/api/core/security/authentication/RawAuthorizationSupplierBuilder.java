package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;

import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import com.garganttua.core.observability.Logger;

@SuppressWarnings("rawtypes")
public class RawAuthorizationSupplierBuilder implements ISupplierBuilder<byte[], IContextualSupplier<byte[], IRuntimeContext>> {
	private static final Logger log = Logger.getLogger(RawAuthorizationSupplierBuilder.class);


    private static final IClass<byte[]> SUPPLIED_CLASS = IClass.getClass(byte[].class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<byte[]> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<byte[], IRuntimeContext> build() throws DslException {
        log.debug("Building RawAuthorizationSupplier");
        return new RawAuthorizationSupplier();
    }

}

package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;

import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import com.garganttua.core.diagnostic.Diagnostics;
import com.garganttua.core.diagnostic.IDiagnostic;

@SuppressWarnings("rawtypes")
public class PrincipalSupplierBuilder implements ISupplierBuilder<Object, IContextualSupplier<Object, IRuntimeContext>> {
	private static final IDiagnostic log = Diagnostics.of(PrincipalSupplierBuilder.class);


    private static final IClass<Object> SUPPLIED_CLASS = IClass.getClass(Object.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<Object> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<Object, IRuntimeContext> build() throws DslException {
        log.debug("Building PrincipalSupplier");
        return new PrincipalSupplier();
    }

}

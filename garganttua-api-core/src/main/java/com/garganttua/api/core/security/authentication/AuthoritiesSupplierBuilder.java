package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.util.List;

import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import com.garganttua.core.observability.Logger;

@SuppressWarnings("rawtypes")
public class AuthoritiesSupplierBuilder implements ISupplierBuilder<List, IContextualSupplier<List, IRuntimeContext>> {
	private static final Logger log = Logger.getLogger(AuthoritiesSupplierBuilder.class);


    private static final IClass<List> SUPPLIED_CLASS = IClass.getClass(List.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<List> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<List, IRuntimeContext> build() throws DslException {
        log.debug("Building AuthoritiesSupplier");
        return new AuthoritiesSupplier();
    }

}

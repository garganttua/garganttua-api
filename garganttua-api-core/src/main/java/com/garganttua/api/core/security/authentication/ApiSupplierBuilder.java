package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;

import com.garganttua.api.commons.context.IApi;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import com.garganttua.core.observability.Logger;

@SuppressWarnings("rawtypes")
public class ApiSupplierBuilder implements ISupplierBuilder<IApi, IContextualSupplier<IApi, IRuntimeContext>> {
	private static final Logger log = Logger.getLogger(ApiSupplierBuilder.class);


    private static final IClass<IApi> SUPPLIED_CLASS = IClass.getClass(IApi.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<IApi> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<IApi, IRuntimeContext> build() throws DslException {
        log.debug("Building ApiSupplier");
        return new ApiSupplier();
    }

}

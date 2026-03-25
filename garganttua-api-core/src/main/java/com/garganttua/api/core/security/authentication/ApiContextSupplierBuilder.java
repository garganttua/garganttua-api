package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;

import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("rawtypes")
public class ApiContextSupplierBuilder implements ISupplierBuilder<IApiContext, IContextualSupplier<IApiContext, IRuntimeContext>> {

    private static final IClass<IApiContext> SUPPLIED_CLASS = IClass.getClass(IApiContext.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<IApiContext> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<IApiContext, IRuntimeContext> build() throws DslException {
        log.atDebug().log("Building ApiContextSupplier");
        return new ApiContextSupplier();
    }

}

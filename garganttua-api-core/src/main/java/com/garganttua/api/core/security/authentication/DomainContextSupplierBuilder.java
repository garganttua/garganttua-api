package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;

import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("rawtypes")
public class DomainContextSupplierBuilder implements ISupplierBuilder<IDomainContext, IContextualSupplier<IDomainContext, IRuntimeContext>> {

    private static final IClass<IDomainContext> SUPPLIED_CLASS = IClass.getClass(IDomainContext.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<IDomainContext> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<IDomainContext, IRuntimeContext> build() throws DslException {
        log.atDebug().log("Building DomainContextSupplier");
        return new DomainContextSupplier();
    }

}

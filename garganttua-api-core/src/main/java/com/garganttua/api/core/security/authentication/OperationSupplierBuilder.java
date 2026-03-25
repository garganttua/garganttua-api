package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;

import com.garganttua.api.spec.operation.OperationDefinition;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("rawtypes")
public class OperationSupplierBuilder implements ISupplierBuilder<OperationDefinition, IContextualSupplier<OperationDefinition, IRuntimeContext>> {

    private static final IClass<OperationDefinition> SUPPLIED_CLASS = IClass.getClass(OperationDefinition.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<OperationDefinition> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<OperationDefinition, IRuntimeContext> build() throws DslException {
        log.atDebug().log("Building OperationSupplier");
        return new OperationSupplier();
    }

}

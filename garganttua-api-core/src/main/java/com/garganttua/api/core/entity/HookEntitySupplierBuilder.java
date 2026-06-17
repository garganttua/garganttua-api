package com.garganttua.api.core.entity;

import java.lang.reflect.Type;

import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

/**
 * Builds a {@link HookEntitySupplier} typed to the domain entity class — the supplier that feeds a
 * free lifecycle-hook method its entity-typed parameter with the current entity. Mirrors
 * {@code UseCaseInputSupplierBuilder}.
 */
@SuppressWarnings("rawtypes")
public class HookEntitySupplierBuilder
        implements ISupplierBuilder<Object, IContextualSupplier<Object, IRuntimeContext>> {

    private final IClass<?> entityType;

    public HookEntitySupplierBuilder(IClass<?> entityType) {
        this.entityType = entityType != null ? entityType : IClass.getClass(Object.class);
    }

    @Override
    public Type getSuppliedType() {
        return this.entityType.getType();
    }

    @SuppressWarnings("unchecked")
    @Override
    public IClass<Object> getSuppliedClass() {
        return (IClass<Object>) this.entityType;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<Object, IRuntimeContext> build() throws DslException {
        return new HookEntitySupplier(this.entityType);
    }
}

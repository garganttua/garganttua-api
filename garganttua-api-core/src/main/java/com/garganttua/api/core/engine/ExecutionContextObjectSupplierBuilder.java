package com.garganttua.api.core.engine;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IContextualObjectSupplierBuilder;
import com.garganttua.api.spec.engine.IExecutionContext;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.api.spec.engine.ISupplyObject;

public class ExecutionContextObjectSupplierBuilder<Supplied>
        implements IContextualObjectSupplierBuilder<Supplied, IExecutionContext> {

    private @Nonnull ISupplyObject<Supplied, IExecutionContext> supply;
    private @Nonnull Class<Supplied> suppliedClass;

    public ExecutionContextObjectSupplierBuilder(ISupplyObject<Supplied, IExecutionContext> supply,
            Class<Supplied> suppliedClass) {
        this.supply = Objects.requireNonNull(supply, "supply cannot be null");
        this.suppliedClass = Objects.requireNonNull(suppliedClass, "Supplied class cannot be null");
    }

    @Override
    public IObjectSupplier<Supplied> build() throws CoreException {
        return new ContextualObjectSupplier<>(this.supply, this.suppliedClass, IExecutionContext.class);
    }

    @Override
    public Class<Supplied> getObjectClass() {
        return this.suppliedClass;
    }
}
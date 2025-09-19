package com.garganttua.api.core.engine;

import java.util.Objects;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IContextualObjectSupplierBuilder;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.api.spec.engine.ISupplyObject;

import lombok.NonNull;

public class ApplicationContextObjectSupplierBuilder<Supplied> implements IContextualObjectSupplierBuilder<Supplied, IApplicationContext> {

    private @NonNull ISupplyObject<Supplied, IApplicationContext> supply;
    private @NonNull Class<Supplied> suppliedClass;

    public ApplicationContextObjectSupplierBuilder(ISupplyObject<Supplied, IApplicationContext> supply, Class<Supplied> suppliedClass) {
        this.supply = Objects.requireNonNull(supply, "supply cannot be null");
        this.suppliedClass = Objects.requireNonNull(suppliedClass, "Supplied class cannot be null");
    }

    @Override
    public IObjectSupplier<Supplied> build() throws CoreException {
        return new ContextualObjectSupplier<>(this.supply, this.suppliedClass, IApplicationContext.class);
    }

    @Override
    public Class<Supplied> getObjectClass() {
       return this.suppliedClass;
    }
}

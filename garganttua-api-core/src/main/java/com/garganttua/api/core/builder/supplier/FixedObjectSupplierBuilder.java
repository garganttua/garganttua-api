package com.garganttua.api.core.builder.supplier;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IExecutionContext;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;

public class FixedObjectSupplierBuilder<T> implements IObjectSupplierBuilder<T> {

    private @Nonnull T object;

    public FixedObjectSupplierBuilder(T object) {
        this.object = Objects.requireNonNull(object, "Cannot supply null object");
    }

    @Override
    public IObjectSupplier<T> build() throws CoreException {
        return new IObjectSupplier<T>() {

            @Override
            public Optional<T> getObject() {
                return Optional.of(object);
            }

            @SuppressWarnings("unchecked")
            @Override
            public Class<T> getObjectClass() {
                return (Class<T>) object.getClass();
            }

            @Override
            public Optional<T> getObject(IApplicationContext aContext, IExecutionContext eContext)
                    throws CoreException {
                return this.getObject();
            }

            @Override
            public Optional<T> getObject(IExecutionContext context) throws CoreException {
                return this.getObject();
            }

            @Override
            public Optional<T> getObject(IApplicationContext context) throws CoreException {
                return this.getObject();
            }

        };
    }

    @Override
    public Class<T> getObjectClass() {
        return (Class<T>) this.object.getClass();
    }

}

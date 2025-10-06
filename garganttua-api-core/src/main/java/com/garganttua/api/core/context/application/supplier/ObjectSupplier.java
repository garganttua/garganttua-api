package com.garganttua.api.core.context.application.supplier;

import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IExecutionContext;
import com.garganttua.api.spec.engine.IObjectSupplier;

public class ObjectSupplier<T> implements IObjectSupplier<T> {

    private T object;
    private Class<T> objectClass;

    @SuppressWarnings("unchecked")
    public ObjectSupplier(T object) {
        this.object = Objects.requireNonNull(object, "Object cannot be null");
        this.objectClass = (Class<T>) object.getClass();
    }

    @Override
    public Optional<T> getObject() {
        return Optional.of(this.object);
    }

    @Override
    public Class<T> getObjectClass() {
        return this.objectClass;
    }

    @Override
    public Optional<T> getObject(IApplicationContext aContext, IExecutionContext eContext) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
    }

    @Override
    public Optional<T> getObject(IExecutionContext context) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
    }

    @Override
    public Optional<T> getObject(IApplicationContext context) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
    }
}

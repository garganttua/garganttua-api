package com.garganttua.api.core.engine;

import java.util.Objects;

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
    public T getObject() {
        return this.object;
    }

    @Override
    public Class<T> getObjectClass() {
        return this.objectClass;
    }

}

package com.garganttua.api.core.context.application.supplier;

import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IExecutionContext;
import com.garganttua.api.spec.engine.IObjectSupplier;

public class EntityObjectSupplier<T> implements IObjectSupplier<T> {

    private T entity;
    private Class<T> entityClass;

    public EntityObjectSupplier(Class<T> entityClass) {
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    public void setObjectToSupply(T entity) throws SupplyException {
        Objects.requireNonNull(entity, "Entity cannot be null");

        if (!this.entityClass.isAssignableFrom(entity.getClass()))
            throw new SupplyException("Type mismatch");

        this.entity = entity;
    }

    @Override
    public Optional<T> getObject() {
        return Optional.ofNullable(this.entity);
    }

    @Override
    public Class<T> getObjectClass() {
        return this.entityClass;
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

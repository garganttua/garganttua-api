package com.garganttua.api.core.engine;

import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.engine.IObjectSupplier;

public class EntityObjectSupplier<T> implements IObjectSupplier<T> {

    private T entity;
    private Class<T> entityClass;

    public EntityObjectSupplier(Class<T> entityClass){
        this.entityClass =  Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    public void setObjectToSupply(T entity) throws EngineException {
        Objects.requireNonNull(entity, "Entity cannot be null");

        if( !this.entityClass.isAssignableFrom(entity.getClass()) )
            throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "Type mismatch");

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

}

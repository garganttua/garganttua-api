package com.garganttua.api.core.context.application;

import java.util.Objects;

import com.garganttua.api.core.definition.EntityDefinition;
import com.garganttua.api.spec.context.IEntityContext;

import lombok.Getter;

public class EntityContext<E> implements IEntityContext<E> {

    @Getter
    private EntityDefinition<E> entityDefinition;

    public EntityContext(
            EntityDefinition<E> entityDefinition) {
        this.entityDefinition = Objects.requireNonNull(entityDefinition, "Entity definition cannot be null");
    }

    @Override
    public String getEntityName() {
        return this.entityDefinition.entityClass().getSimpleName().toLowerCase();
    }

    @Override
    public Class<E> getEntityClass() {
        return this.entityDefinition.entityClass();
    }

}

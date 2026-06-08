package com.garganttua.api.core.entity;

import java.util.Objects;

import com.garganttua.api.core.entity.EntityDefinition;
import com.garganttua.api.commons.context.IEntityContext;
import com.garganttua.core.reflection.IClass;

public class EntityContext<E> implements IEntityContext<E> {

    private EntityDefinition<E> entityDefinition;

    public EntityDefinition<E> getEntityDefinition() { return this.entityDefinition; }

    public EntityContext(
            EntityDefinition<E> entityDefinition) {
        this.entityDefinition = Objects.requireNonNull(entityDefinition, "Entity definition cannot be null");
    }

    @Override
    public String getEntityName() {
        return this.entityDefinition.entityClass().getSimpleName().toLowerCase();
    }

    @Override
    public IClass<E> getEntityClass() {
        return this.entityDefinition.entityClass();
    }

}

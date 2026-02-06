package com.garganttua.api.spec.context;

import com.garganttua.api.spec.definition.IEntityDefinition;

public interface IEntityContext<E> {

    IEntityDefinition<E> getEntityDefinition();

    String getEntityName();

    Class<E> getEntityClass();

}

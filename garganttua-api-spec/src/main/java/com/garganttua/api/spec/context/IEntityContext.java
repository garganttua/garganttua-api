package com.garganttua.api.spec.context;

import com.garganttua.api.spec.definition.IEntityDefinition;
import com.garganttua.core.reflection.IClass;

public interface IEntityContext<E> {

    IEntityDefinition<E> getEntityDefinition();

    String getEntityName();

    IClass<E> getEntityClass();

}

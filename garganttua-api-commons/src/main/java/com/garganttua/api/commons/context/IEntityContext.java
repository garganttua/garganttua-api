package com.garganttua.api.commons.context;

import com.garganttua.api.commons.definition.IEntityDefinition;
import com.garganttua.core.reflection.IClass;

public interface IEntityContext<E> {

    IEntityDefinition<E> getEntityDefinition();

    String getEntityName();

    IClass<E> getEntityClass();

}

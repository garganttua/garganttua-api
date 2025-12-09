package com.garganttua.api.spec.context;

public interface IEntityContext<E> {

    String getEntityName();

    Class<E> getEntityClass();

}

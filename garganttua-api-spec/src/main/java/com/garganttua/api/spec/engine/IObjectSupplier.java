package com.garganttua.api.spec.engine;

import java.util.Optional;

public interface IObjectSupplier<ObjectType> {

    Optional<ObjectType> getObject();

    Class<ObjectType> getObjectClass();

}

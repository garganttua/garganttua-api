package com.garganttua.api.spec.engine;

import java.util.Optional;

@FunctionalInterface
public interface ISupplyObject<ObjectType, ContextType> {

    Optional<ObjectType> supplyObject(ContextType context);

}

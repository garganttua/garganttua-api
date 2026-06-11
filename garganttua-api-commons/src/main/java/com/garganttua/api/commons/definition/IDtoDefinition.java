package com.garganttua.api.commons.definition;

import java.util.List;

import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;

public interface IDtoDefinition<D> {

    IClass<D> dtoClass();

    ObjectAddress uuid();

    ObjectAddress id();

    ObjectAddress tenantId();

    /**
     * DTO composition references (à la {@code @DBRef}): each names a field that holds a
     * reference (or list of references) to DTOs stored in another collection. {@code empty}
     * when the DTO composes nothing. Declared via {@code @Composed} / {@code .composed(...)}.
     */
    default List<DtoComposition> compositions() {
        return List.of();
    }

}

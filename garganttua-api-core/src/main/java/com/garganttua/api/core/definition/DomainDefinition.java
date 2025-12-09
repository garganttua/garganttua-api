package com.garganttua.api.core.definition;

import java.util.List;
import java.util.Map;

import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IMethodBinder;

public record DomainDefinition<E> (
    String domainName,
    EntityDefinition<E> entityDefinition,
    List<DtoDefinition<?>> dtoDefinitions,
    List<IMethodBinder<Void>> startupBinderBuilders,
    Boolean activateCreation,
    Boolean activateAllowReadAll,
    Boolean activateReadOne,
    Boolean activateUpdate,
    Boolean activateDeleteAll,
    Boolean activateDeleteOne,
    Boolean publik,
    Boolean tenant,
    List<E> createEntities,
    List<E> upsertEntities,
    ObjectAddress owner,
    ObjectAddress owned,
    ObjectAddress shared,
    ObjectAddress hiddenable,
    Map<String, UseCaseDefinition> useCases
    ) {
}

package com.garganttua.api.core.definition;

import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IMethodBinderBuilder;
import com.garganttua.api.spec.engine.IUseCaseBuilder;
import com.garganttua.reflection.GGObjectAddress;

public record DomainDefinition (
    String domainName,
    List<IMethodBinderBuilder<?, ?>> startupBinderBuilders,
    Boolean activateCreation,
    Boolean activateAllowReadAll,
    Boolean activateReadOne,
    Boolean activateUpdate,
    Boolean activateDeleteAll,
    Boolean activateDeleteOne,
    Boolean publik,
    Boolean tenant,
    List<Object> createEntities,
    List<Object> upsertEntities,
    GGObjectAddress owner,
    GGObjectAddress owned,
    GGObjectAddress shared,
    GGObjectAddress hiddenable,
    Map<String, IUseCaseBuilder<IDomainBuilder>>  useCases
    ) {
}

package com.garganttua.api.core.context.application;

import java.util.List;
import java.util.Objects;

import com.garganttua.api.core.definition.DomainEntityDefinition;
import com.garganttua.api.spec.engine.IDomainEntityContext;
import com.garganttua.api.spec.engine.IMethodBinderBuilder;

public class DomainEntityContext implements IDomainEntityContext {

    private DomainEntityDefinition entityDefinition;

    public DomainEntityContext(
            DomainEntityDefinition entityDefinition) {
        this.entityDefinition = Objects.requireNonNull(entityDefinition, "Entity definition cannot be null");
    }

    @Override
    public String getEntityName() {
        return this.entityDefinition.entityClass().getSimpleName().toLowerCase();
    }

    @Override
    public Class<?> getEntityClass() {
        return this.entityDefinition.entityClass();
    }

    @Override
    public List<IMethodBinderBuilder<?, ?>> getAfterGetMethods() {
        return this.entityDefinition.afterGetMethodBuilders();
    }

}

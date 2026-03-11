package com.garganttua.api.core.definition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.garganttua.api.spec.operation.Access;
import com.garganttua.api.spec.operation.BusinessOperation;
import com.garganttua.api.spec.operation.OperationDefinition;
import com.garganttua.api.spec.operation.Scope;
import com.garganttua.api.spec.operation.TechnicalOperation;
import com.garganttua.api.spec.definition.IDomainDefinition;
import com.garganttua.api.spec.definition.IDomainSecurityDefinition;
import com.garganttua.api.spec.definition.IDtoDefinition;
import com.garganttua.api.spec.definition.IEntityDefinition;
import com.garganttua.api.spec.definition.IUseCaseDefinition;
import com.garganttua.api.spec.definition.IWorkflowDefinition;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IMethodBinder;

public record DomainDefinition<E>(
        String domainName,
        IEntityDefinition<E> entityDefinition,
        List<IDtoDefinition<E>> dtoDefinitions,
        List<IMethodBinder<Void>> startupBinders,
        Boolean publik,
        Boolean tenant,
        List<E> createEntities,
        List<E> upsertEntities,
        ObjectAddress owner,
        ObjectAddress owned,
        ObjectAddress shared,
        ObjectAddress hiddenable,
        ObjectAddress geolocalized,
        Map<String, IUseCaseDefinition> useCases,
        Map<String, IWorkflowDefinition> workflows,
        IDomainSecurityDefinition domainSecurityDefinition) implements IDomainDefinition<E> {

    @Override
    public List<OperationDefinition> operations() {
        List<OperationDefinition> ops = new ArrayList<>();
        IClass<?> entityClass = entityDefinition.entityClass();

        collectCrudOperations(ops, entityClass);
        collectWorkflowOperations(ops, entityClass);
        collectUseCaseOperations(ops, entityClass);

        return ops;
    }

    private void collectCrudOperations(List<OperationDefinition> ops, IClass<?> entityClass) {
        if (workflows == null) return;
        addCrudIfPresent(ops, BusinessOperation.create, OperationDefinition::createOne, entityClass);
        addCrudIfPresent(ops, BusinessOperation.readAll, OperationDefinition::readAll, entityClass);
        addCrudIfPresent(ops, BusinessOperation.readOne, OperationDefinition::readOne, entityClass);
        addCrudIfPresent(ops, BusinessOperation.update, OperationDefinition::updateOne, entityClass);
        addCrudIfPresent(ops, BusinessOperation.deleteOne, OperationDefinition::deleteOne, entityClass);
        addCrudIfPresent(ops, BusinessOperation.deleteAll, OperationDefinition::deleteAll, entityClass);
    }

    @FunctionalInterface
    private interface CrudFactory {
        OperationDefinition create(String domainName, IClass<?> entityClass, boolean authority, Access access);
    }

    private void addCrudIfPresent(List<OperationDefinition> ops, BusinessOperation bo, CrudFactory f, IClass<?> entityClass) {
        IWorkflowDefinition wfDef = workflows.get(bo.getLabel());
        if (wfDef != null) {
            Access access = wfDef.access() != null ? wfDef.access() : Access.authenticated;
            boolean authority = wfDef.authority();
            ops.add(f.create(domainName, entityClass, authority, access));
        }
    }

    private void collectWorkflowOperations(List<OperationDefinition> ops, IClass<?> entityClass) {
        if (workflows == null) return;
        for (IWorkflowDefinition wfDef : workflows.values()) {
            if (!wfDef.custom()) continue;
            ops.add(OperationDefinition.workflow(domainName,
                    Objects.requireNonNullElse(wfDef.operation(), TechnicalOperation.read),
                    entityClass,
                    Objects.requireNonNullElse(wfDef.scope(), Scope.allEntities),
                    wfDef.authority(), wfDef.access()));
        }
    }

    private void collectUseCaseOperations(List<OperationDefinition> ops, IClass<?> entityClass) {
        if (useCases == null) return;
        for (IUseCaseDefinition ucDef : useCases.values()) {
            ops.add(OperationDefinition.useCase(domainName,
                    Objects.requireNonNullElse(ucDef.operation(), TechnicalOperation.read),
                    entityClass,
                    Objects.requireNonNullElse(ucDef.scope(), Scope.allEntities),
                    ucDef.authority(), ucDef.access()));
        }
    }

}

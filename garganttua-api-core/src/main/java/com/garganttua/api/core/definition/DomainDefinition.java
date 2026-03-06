package com.garganttua.api.core.definition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.BusinessOperation;
import com.garganttua.api.spec.context.Operation;
import com.garganttua.api.spec.context.Scope;
import com.garganttua.api.spec.context.TechnicalOperation;
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
        Map<String, IUseCaseDefinition> useCases,
        Map<String, IWorkflowDefinition> workflows,
        IDomainSecurityDefinition domainSecurityDefinition) implements IDomainDefinition<E> {

    @Override
    public List<Operation> operations() {
        List<Operation> ops = new ArrayList<>();
        IClass<?> entityClass = entityDefinition.entityClass();

        collectCrudOperations(ops, entityClass);
        collectWorkflowOperations(ops, entityClass);
        collectUseCaseOperations(ops, entityClass);

        return ops;
    }

    private void collectCrudOperations(List<Operation> ops, IClass<?> entityClass) {
        if (workflows == null) return;
        addCrudIfPresent(ops, BusinessOperation.create, Operation::createOne, entityClass);
        addCrudIfPresent(ops, BusinessOperation.readAll, Operation::readAll, entityClass);
        addCrudIfPresent(ops, BusinessOperation.readOne, Operation::readOne, entityClass);
        addCrudIfPresent(ops, BusinessOperation.update, Operation::updateOne, entityClass);
        addCrudIfPresent(ops, BusinessOperation.deleteOne, Operation::deleteOne, entityClass);
        addCrudIfPresent(ops, BusinessOperation.deleteAll, Operation::deleteAll, entityClass);
    }

    @FunctionalInterface
    private interface CrudFactory {
        Operation create(String domainName, IClass<?> entityClass, boolean authority, Access access);
    }

    private void addCrudIfPresent(List<Operation> ops, BusinessOperation bo, CrudFactory f, IClass<?> entityClass) {
        IWorkflowDefinition wfDef = workflows.get(bo.getLabel());
        if (wfDef != null) {
            Access access = wfDef.access() != null ? wfDef.access() : Access.authenticated;
            boolean authority = wfDef.authority();
            ops.add(f.create(domainName, entityClass, authority, access));
        }
    }

    private void collectWorkflowOperations(List<Operation> ops, IClass<?> entityClass) {
        if (workflows == null) return;
        for (IWorkflowDefinition wfDef : workflows.values()) {
            if (!wfDef.custom()) continue;
            ops.add(Operation.workflow(domainName,
                    Objects.requireNonNullElse(wfDef.operation(), TechnicalOperation.read),
                    entityClass,
                    Objects.requireNonNullElse(wfDef.scope(), Scope.allEntities),
                    wfDef.authority(), wfDef.access()));
        }
    }

    private void collectUseCaseOperations(List<Operation> ops, IClass<?> entityClass) {
        if (useCases == null) return;
        for (IUseCaseDefinition ucDef : useCases.values()) {
            ops.add(Operation.useCase(domainName,
                    Objects.requireNonNullElse(ucDef.operation(), TechnicalOperation.read),
                    entityClass,
                    Objects.requireNonNullElse(ucDef.scope(), Scope.allEntities),
                    ucDef.authority(), ucDef.access()));
        }
    }

}

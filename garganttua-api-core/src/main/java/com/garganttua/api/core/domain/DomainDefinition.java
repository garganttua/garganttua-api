package com.garganttua.api.core.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.operation.Scope;
import com.garganttua.api.commons.operation.TechnicalOperation;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.definition.IDomainKeyDefinition;
import com.garganttua.api.commons.definition.IDomainSecurityDefinition;
import com.garganttua.api.commons.definition.IDtoDefinition;
import com.garganttua.api.commons.definition.IEntityDefinition;
import com.garganttua.api.commons.definition.IUseCaseDefinition;
import com.garganttua.api.commons.definition.IWorkflowDefinition;
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
        ObjectAddress superOwner,
        ObjectAddress superTenant,
        Map<String, IUseCaseDefinition> useCases,
        Map<String, IWorkflowDefinition> workflows,
        IDomainSecurityDefinition domainSecurityDefinition,
        IDomainKeyDefinition keyDefinition) implements IDomainDefinition<E> {

    @Override
    public List<OperationDefinition> operations() {
        List<OperationDefinition> ops = new ArrayList<>();
        IClass<?> entityClass = entityDefinition.entityClass();

        collectCrudOperations(ops, entityClass);
        collectAuthOperations(ops, entityClass);
        collectWorkflowOperations(ops, entityClass);
        collectUseCaseOperations(ops, entityClass);

        return ops;
    }

    /**
     * Exposes the auto-registered authentication operations (authenticate / refresh)
     * so transports can route them like any other operation. They live in the
     * {@code workflows} map under their business-operation labels but are
     * {@code custom() == false}, so {@link #collectWorkflowOperations} skips them —
     * hence this dedicated collector. Both are anonymous entry points
     * ({@code Access.anonymous}, no authority).
     */
    private void collectAuthOperations(List<OperationDefinition> ops, IClass<?> entityClass) {
        if (workflows == null) return;
        if (workflows.containsKey(BusinessOperation.authenticate.getLabel())) {
            ops.add(OperationDefinition.authenticate(domainName, entityClass));
        }
        if (workflows.containsKey(BusinessOperation.refreshAuthorization.getLabel())) {
            ops.add(OperationDefinition.refreshAuthorization(domainName, entityClass));
        }
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
        OperationDefinition create(String domainName, IClass<?> entityClass, boolean authority, String authorityName, Access access);
    }

    private void addCrudIfPresent(List<OperationDefinition> ops, BusinessOperation bo, CrudFactory f, IClass<?> entityClass) {
        IWorkflowDefinition wfDef = workflows.get(bo.getLabel());
        if (wfDef != null) {
            Access access = wfDef.access() != null ? wfDef.access() : Access.authenticated;
            boolean authority = wfDef.authority();
            ops.add(f.create(domainName, entityClass, authority, wfDef.authorityName(), access));
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
                    wfDef.authority(), wfDef.authorityName(), wfDef.access()));
        }
    }

    private void collectUseCaseOperations(List<OperationDefinition> ops, IClass<?> entityClass) {
        if (useCases == null) return;
        for (IUseCaseDefinition ucDef : useCases.values()) {
            // The use-case definition (verb/scope/access/authority + path/in-out/binder) is carried
            // on the operation; the builder guarantees a non-null verb (read) and scope (allEntities).
            ops.add(OperationDefinition.useCase(domainName, entityClass, ucDef));
        }
    }

}

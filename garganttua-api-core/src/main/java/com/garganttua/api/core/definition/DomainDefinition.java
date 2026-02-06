package com.garganttua.api.core.definition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.Operation;
import com.garganttua.api.spec.context.Scope;
import com.garganttua.api.spec.context.TechnicalOperation;
import com.garganttua.api.spec.definition.IDomainDefinition;
import com.garganttua.api.spec.definition.IDomainSecurityDefinition;
import com.garganttua.api.spec.definition.IDtoDefinition;
import com.garganttua.api.spec.definition.IEntityDefinition;
import com.garganttua.api.spec.definition.IUseCaseDefinition;
import com.garganttua.api.spec.definition.IWorkflowDefinition;
import com.garganttua.api.spec.security.IAccessRule;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IMethodBinder;

public record DomainDefinition<E> (
    String domainName,
    IEntityDefinition<E> entityDefinition,
    IDomainSecurityDefinition securityDefinition,
    List<? extends IDtoDefinition<?>> dtoDefinitions,
    List<IMethodBinder<Void>> startupBinders,
    Boolean activateCreation,
    Boolean activateReadAll,
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
    Map<String, ? extends IUseCaseDefinition> useCases,
    Map<String, ? extends IWorkflowDefinition> workflows
    ) implements IDomainDefinition<E> {

    @Override
    public List<IAccessRule> accessRules() {
        List<IAccessRule> rules = new ArrayList<>();
        Class<?> entityClass = entityDefinition.entityClass();

        if (Boolean.TRUE.equals(activateCreation)) {
            Operation op = Operation.createOne(domainName, entityClass);
            rules.add(createAccessRule(op,
                getAccess(securityDefinition, securityDefinition != null ? securityDefinition.creationAccess() : null),
                securityDefinition != null && Boolean.TRUE.equals(securityDefinition.creationAuthority())));
        }
        if (Boolean.TRUE.equals(activateReadAll)) {
            Operation op = Operation.readAll(domainName, entityClass);
            rules.add(createAccessRule(op,
                getAccess(securityDefinition, securityDefinition != null ? securityDefinition.readAllAccess() : null),
                securityDefinition != null && Boolean.TRUE.equals(securityDefinition.readAllAuthority())));
        }
        if (Boolean.TRUE.equals(activateReadOne)) {
            Operation op = Operation.readOne(domainName, entityClass);
            rules.add(createAccessRule(op,
                getAccess(securityDefinition, securityDefinition != null ? securityDefinition.readOneAccess() : null),
                securityDefinition != null && Boolean.TRUE.equals(securityDefinition.readOneAuthority())));
        }
        if (Boolean.TRUE.equals(activateUpdate)) {
            Operation op = Operation.updateOne(domainName, entityClass);
            rules.add(createAccessRule(op,
                getAccess(securityDefinition, securityDefinition != null ? securityDefinition.updateAccess() : null),
                securityDefinition != null && Boolean.TRUE.equals(securityDefinition.updateAuthority())));
        }
        if (Boolean.TRUE.equals(activateDeleteOne)) {
            Operation op = Operation.deleteOne(domainName, entityClass);
            rules.add(createAccessRule(op,
                getAccess(securityDefinition, securityDefinition != null ? securityDefinition.deleteOneAccess() : null),
                securityDefinition != null && Boolean.TRUE.equals(securityDefinition.deleteOneAuthority())));
        }
        if (Boolean.TRUE.equals(activateDeleteAll)) {
            Operation op = Operation.deleteAll(domainName, entityClass);
            rules.add(createAccessRule(op,
                getAccess(securityDefinition, securityDefinition != null ? securityDefinition.deleteAllAccess() : null),
                securityDefinition != null && Boolean.TRUE.equals(securityDefinition.deleteAllAuthority())));
        }

        // Generate access rules for custom workflows
        if (workflows != null) {
            for (IWorkflowDefinition wfDef : workflows.values()) {
                if (wfDef.custom()) {
                    TechnicalOperation techOp = wfDef.operation() != null ? wfDef.operation() : TechnicalOperation.read;
                    Scope scope = wfDef.scope() != null ? wfDef.scope() : Scope.allEntities;
                    Operation op = Operation.workflow(domainName, techOp, entityClass, scope);
                    rules.add(createAccessRule(op, wfDef.access(), wfDef.authority()));
                }
            }
        }

        return rules;
    }

    private Access getAccess(IDomainSecurityDefinition security, Access specificAccess) {
        if (specificAccess != null) {
            return specificAccess;
        }
        if (Boolean.TRUE.equals(publik)) {
            return Access.anonymous;
        }
        if (Boolean.TRUE.equals(tenant)) {
            return Access.tenant;
        }
        return Access.authenticated;
    }

    private AccessRule createAccessRule(Operation operation, Access access, boolean withAuthority) {
        String authority = withAuthority ? operation.getOperationName().toUpperCase().replace("-", "_") : null;
        return new AccessRule(operation, authority, access);
    }

}

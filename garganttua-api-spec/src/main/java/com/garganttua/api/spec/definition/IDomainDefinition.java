package com.garganttua.api.spec.definition;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.garganttua.api.spec.security.IAccessRule;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IMethodBinder;

public interface IDomainDefinition<E> {

    String domainName();

    IEntityDefinition<E> entityDefinition();

    List<? extends IDtoDefinition<?>> dtoDefinitions();

    List<IAccessRule> accessRules();

    default Optional<IAccessRule> accessRule(String key) {
        return this.accessRules().stream().filter(r -> key.equals(r.key())).findAny();
    }

    Boolean publik();

    Boolean tenant();

    List<E> createEntities();

    List<E> upsertEntities();

    ObjectAddress owner();

    ObjectAddress owned();

    ObjectAddress shared();

    ObjectAddress hiddenable();

    @Deprecated
    Map<String, ? extends IUseCaseDefinition> useCases();

    Map<String, ? extends IWorkflowDefinition> workflows();

    List<IMethodBinder<Void>> startupBinders();

    // Activation flags for CRUD operations
    Boolean activateCreation();

    Boolean activateReadAll();

    Boolean activateReadOne();

    Boolean activateUpdate();

    Boolean activateDeleteAll();

    Boolean activateDeleteOne();

    // Security definition
    IDomainSecurityDefinition securityDefinition();

}

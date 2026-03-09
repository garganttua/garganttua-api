package com.garganttua.api.spec.definition;

import java.util.List;

import com.garganttua.api.spec.operation.Operation;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IMethodBinder;

public interface IDomainDefinition<E> {

    String domainName();

    IEntityDefinition<E> entityDefinition();

    List<IDtoDefinition<E>> dtoDefinitions();

    List<Operation> operations();

    Boolean publik();

    Boolean tenant();

    List<E> createEntities();

    List<E> upsertEntities();

    ObjectAddress owner();

    ObjectAddress owned();

    ObjectAddress shared();

    ObjectAddress hiddenable();

    List<IMethodBinder<Void>> startupBinders();
}

package com.garganttua.api.commons.definition;

import java.util.List;

import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IMethodBinder;

public interface IDomainDefinition<E> {

    String domainName();

    IEntityDefinition<E> entityDefinition();

    List<IDtoDefinition<E>> dtoDefinitions();

    List<OperationDefinition> operations();

    Boolean publik();

    Boolean tenant();

    List<E> createEntities();

    List<E> upsertEntities();

    ObjectAddress owner();

    ObjectAddress owned();

    ObjectAddress shared();

    ObjectAddress hiddenable();

    ObjectAddress geolocalized();

    List<IMethodBinder<Void>> startupBinders();
}

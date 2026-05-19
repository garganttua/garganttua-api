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

    ObjectAddress superOwner();

    ObjectAddress superTenant();

    List<IMethodBinder<Void>> startupBinders();

    /**
     * Field-layout descriptor for the key-role entity, or {@code null} when
     * this domain is not marked as a key domain. Populated by the entity-role
     * scanner when {@code @Key} is detected on the entity class, or by the
     * DSL {@code .key()} sub-builder.
     */
    IDomainKeyDefinition keyDefinition();
}

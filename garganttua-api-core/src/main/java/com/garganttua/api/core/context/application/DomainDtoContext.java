package com.garganttua.api.core.context.application;

import java.util.Objects;

import com.garganttua.api.core.definition.DomainDtoDefinition;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.engine.IDomainDtoContext;
import com.garganttua.api.spec.engine.IObjectSupplier;

public class DomainDtoContext implements IDomainDtoContext {

    private DomainDtoDefinition domainDtoDefinition;
    private IObjectSupplier<?> daos;

    public DomainDtoContext(DomainDtoDefinition domainDtoDefinition, IObjectSupplier<?> daos) {
        this.domainDtoDefinition = Objects.requireNonNull(domainDtoDefinition, "Dto definition cannot be null");
        this.daos = Objects.requireNonNull(daos, "Dao supplier cannot be null");
    }

    @Override
    public IDao getDao() throws CoreException {
        return (IDao) this.daos.getObject().get();
    }

    @Override
    public String getUuid(Object object) throws CoreException {
        return ObjectAccessor.getValue(object, domainDtoDefinition, DomainDtoDefinition::uuid);
    }
}

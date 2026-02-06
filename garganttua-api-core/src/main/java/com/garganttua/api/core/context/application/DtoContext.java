package com.garganttua.api.core.context.application;

import java.util.Objects;

import com.garganttua.api.core.definition.DtoDefinition;
import com.garganttua.api.spec.context.IDtoContext;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.core.CoreException;
import com.garganttua.core.reflection.ObjectAccessor;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import lombok.Getter;

public class DtoContext<D> implements IDtoContext<D> {

    @Getter
    private DtoDefinition<D> dtoDefinition;
    private ISupplierBuilder<? extends IDao, ISupplier<? extends IDao>> dao;

    public DtoContext(DtoDefinition<D> dtoDefinition, ISupplierBuilder<? extends IDao, ISupplier<? extends IDao>> dao) {
        this.dtoDefinition = Objects.requireNonNull(dtoDefinition, "Dto definition cannot be null");
        this.dao = Objects.requireNonNull(dao, "Dao supplier cannot be null");
    }

    @Override
    public IDao getDao() throws CoreException {
        return this.dao.build().supply().get();
    }

    @Override
    public String getUuid(Object object) throws CoreException {
        return ObjectAccessor.getValue(object, dtoDefinition, DtoDefinition::uuid);
    }
}

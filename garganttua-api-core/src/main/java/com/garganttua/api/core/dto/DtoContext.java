package com.garganttua.api.core.dto;

import java.util.Objects;

import com.garganttua.api.core.dto.DtoDefinition;
import com.garganttua.api.commons.context.IDtoContext;
import com.garganttua.api.commons.dao.IDao;
import com.garganttua.api.commons.ApiException;
import java.util.List;

import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public class DtoContext<D> implements IDtoContext<D> {

    private DtoDefinition<D> dtoDefinition;
    private ISupplierBuilder<? extends IDao, ISupplier<? extends IDao>> dao;

    public DtoDefinition<D> getDtoDefinition() { return this.dtoDefinition; }

    public DtoContext(DtoDefinition<D> dtoDefinition, ISupplierBuilder<? extends IDao, ISupplier<? extends IDao>> dao) {
        this.dtoDefinition = Objects.requireNonNull(dtoDefinition, "Dto definition cannot be null");
        this.dao = Objects.requireNonNull(dao, "Dao supplier cannot be null");
    }

    @Override
    public IDao getDao() throws ApiException {
        return this.dao.build().supply().get();
    }

    private static final IReflection REFLECTION = DefaultMapper.reflection();

    @Override
    public String getUuid(Object object) throws ApiException {
        try {
            ObjectAddress uuidAddress = dtoDefinition.uuid();
            Object value = REFLECTION.getFieldValue(object, uuidAddress.toString());
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            throw new ApiException("Failed to get uuid from DTO", e);
        }
    }
}

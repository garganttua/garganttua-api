package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;

import com.garganttua.api.commons.repository.IRepository;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("rawtypes")
public class RepositorySupplierBuilder implements ISupplierBuilder<IRepository, IContextualSupplier<IRepository, IRuntimeContext>> {

    private static final IClass<IRepository> SUPPLIED_CLASS = IClass.getClass(IRepository.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<IRepository> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<IRepository, IRuntimeContext> build() throws DslException {
        log.atDebug().log("Building RepositorySupplier");
        return new RepositorySupplier();
    }

}

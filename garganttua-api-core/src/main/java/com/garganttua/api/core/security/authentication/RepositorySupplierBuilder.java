package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;

import com.garganttua.api.commons.repository.IRepository;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import com.garganttua.core.observability.Logger;

@SuppressWarnings("rawtypes")
public class RepositorySupplierBuilder implements ISupplierBuilder<IRepository, IContextualSupplier<IRepository, IRuntimeContext>> {
	private static final Logger log = Logger.getLogger(RepositorySupplierBuilder.class);


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
        log.debug("Building RepositorySupplier");
        return new RepositorySupplier();
    }

}

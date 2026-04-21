package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;

import com.garganttua.api.commons.security.authorization.IAuthorization;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("rawtypes")
public class AuthorizationSupplierBuilder implements ISupplierBuilder<IAuthorization, IContextualSupplier<IAuthorization, IRuntimeContext>> {

    private static final IClass<IAuthorization> SUPPLIED_CLASS = IClass.getClass(IAuthorization.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<IAuthorization> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<IAuthorization, IRuntimeContext> build() throws DslException {
        log.atDebug().log("Building AuthorizationSupplier");
        return new AuthorizationSupplier();
    }

}

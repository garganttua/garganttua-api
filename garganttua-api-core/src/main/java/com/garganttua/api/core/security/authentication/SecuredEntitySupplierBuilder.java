package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;

import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import com.garganttua.core.observability.Logger;

/**
 * Builds a {@link SecuredEntitySupplier} — injects the entity being created/updated
 * into a custom {@code applySecurityOnEntity} method parameter.
 */
@SuppressWarnings("rawtypes")
public class SecuredEntitySupplierBuilder
        implements ISupplierBuilder<Object, IContextualSupplier<Object, IRuntimeContext>> {
    private static final Logger log = Logger.getLogger(SecuredEntitySupplierBuilder.class);

    private static final IClass<Object> SUPPLIED_CLASS = IClass.getClass(Object.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<Object> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<Object, IRuntimeContext> build() throws DslException {
        log.debug("Building SecuredEntitySupplier");
        return new SecuredEntitySupplier();
    }

}

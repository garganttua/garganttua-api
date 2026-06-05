package com.garganttua.api.core.security.authorization;

import java.lang.reflect.Type;

import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import com.garganttua.core.observability.Logger;

/**
 * Builds a {@link RequestSupplier} — injects the in-flight
 * {@link IOperationRequest} into a custom issuer method parameter.
 */
public class RequestSupplierBuilder
        implements ISupplierBuilder<IOperationRequest, IContextualSupplier<IOperationRequest, IRuntimeContext>> {
    private static final Logger log = Logger.getLogger(RequestSupplierBuilder.class);

    private static final IClass<IOperationRequest> SUPPLIED_CLASS = IClass.getClass(IOperationRequest.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<IOperationRequest> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<IOperationRequest, IRuntimeContext> build() throws DslException {
        log.debug("Building RequestSupplier");
        return new RequestSupplier();
    }

}

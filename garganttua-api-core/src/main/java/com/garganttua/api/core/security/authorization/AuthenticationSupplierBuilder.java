package com.garganttua.api.core.security.authorization;

import java.lang.reflect.Type;

import com.garganttua.api.commons.security.authentication.IAuthentication;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import com.garganttua.core.observability.Logger;

/**
 * Builds an {@link AuthenticationSupplier} — injects the successful
 * {@link IAuthentication} result into a custom issuer method parameter.
 */
public class AuthenticationSupplierBuilder
        implements ISupplierBuilder<IAuthentication, IContextualSupplier<IAuthentication, IRuntimeContext>> {
    private static final Logger log = Logger.getLogger(AuthenticationSupplierBuilder.class);

    private static final IClass<IAuthentication> SUPPLIED_CLASS = IClass.getClass(IAuthentication.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<IAuthentication> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<IAuthentication, IRuntimeContext> build() throws DslException {
        log.debug("Building AuthenticationSupplier");
        return new AuthenticationSupplier();
    }

}

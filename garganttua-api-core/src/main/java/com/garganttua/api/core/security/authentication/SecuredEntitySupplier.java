package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.util.Optional;

import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

import com.garganttua.core.observability.Logger;

/**
 * Injects the entity being CREATED or UPDATED into a custom
 * {@code applySecurityOnEntity} method parameter. Reads the CURRENT pipeline entity from
 * the runtime {@code "entity"} variable (published by the applySecurityOnEntity expression
 * after uuid/tenant/owner stamping), so the user method can secure it in place (e.g. hash a
 * password field) or return a secured copy.
 *
 * <p>The {@code applySecurityOnEntity} method imposes no parameter — wire this supplier
 * explicitly when the method needs the entity:
 * {@code .applySecurityOnEntity("m").withParam(0, new SecuredEntitySupplierBuilder())}.
 */
@SuppressWarnings("rawtypes")
public class SecuredEntitySupplier implements IContextualSupplier<Object, IRuntimeContext> {
    private static final Logger log = Logger.getLogger(SecuredEntitySupplier.class);

    private static final IClass<Object> SUPPLIED_CLASS = IClass.getClass(Object.class);
    private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<Object> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public IClass<IRuntimeContext> getOwnerContextType() {
        return CONTEXT_CLASS;
    }

    @Override
    public Optional<Object> supply(IRuntimeContext context, Object... otherContexts) throws SupplyException {
        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }
        // The entity is published by the applySecurityOnEntity expression as the CURRENT
        // pipeline entity (after uuid/tenant/owner stamping), not the raw request body.
        Optional<?> entityOpt = context.getVariable("entity", IClass.getClass(Object.class));
        if (entityOpt.isEmpty()) {
            throw new SupplyException("Variable 'entity' not found in runtime context");
        }
        log.debug("SecuredEntitySupplier resolved the entity to secure");
        return Optional.ofNullable(entityOpt.get());
    }

}

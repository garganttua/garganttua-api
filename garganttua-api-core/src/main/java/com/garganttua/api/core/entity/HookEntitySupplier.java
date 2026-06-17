package com.garganttua.api.core.entity;

import java.lang.reflect.Type;
import java.util.Optional;

import com.garganttua.core.observability.Logger;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

/**
 * Supplies a free lifecycle-hook's bound method with the CURRENT entity — the one being
 * created/updated/deleted/read. Reads the runtime {@code "entity"} variable seeded by the lifecycle
 * expressions when they execute the hook binder (mirrors {@code SecuredEntitySupplier}). Typed to the
 * domain entity class so it wires the hook method's entity-typed parameter (e.g. a free
 * {@code static void validate(User u)}). Mirrors {@code UseCaseInputSupplier}.
 */
@SuppressWarnings("rawtypes")
public class HookEntitySupplier implements IContextualSupplier<Object, IRuntimeContext> {

    private static final Logger log = Logger.getLogger(HookEntitySupplier.class);
    private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

    private final IClass<?> entityType;

    public HookEntitySupplier(IClass<?> entityType) {
        this.entityType = entityType != null ? entityType : IClass.getClass(Object.class);
    }

    @Override
    public Type getSuppliedType() {
        return this.entityType.getType();
    }

    @SuppressWarnings("unchecked")
    @Override
    public IClass<Object> getSuppliedClass() {
        return (IClass<Object>) this.entityType;
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
        Optional<?> entityOpt = context.getVariable("entity", IClass.getClass(Object.class));
        if (entityOpt.isEmpty()) {
            throw new SupplyException("Variable 'entity' not found in runtime context");
        }
        log.debug("HookEntitySupplier resolved the current entity (present={})", entityOpt.get() != null);
        return Optional.ofNullable(entityOpt.get());
    }
}

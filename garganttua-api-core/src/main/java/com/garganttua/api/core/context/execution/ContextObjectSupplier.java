package com.garganttua.api.core.context.execution;

import com.garganttua.core.supply.IObjectSupplier;

import lombok.extern.slf4j.Slf4j;

/**
 * Object supplier that retrieves objects from the execution context.
 * This is used to supply runtime objects during method binding execution.
 *
 * @param <T> the type of object to supply
 */
@Slf4j
public class ContextObjectSupplier<T> implements IObjectSupplier<T> {

    private final Class<T> type;
    private final String contextKey;

    /**
     * Creates a context object supplier.
     *
     * @param type the type of object to supply
     * @param contextKey the key in the execution context
     */
    public ContextObjectSupplier(Class<T> type, String contextKey) {
        this.type = type;
        this.contextKey = contextKey;
        log.atTrace().log("[ContextObjectSupplier] Created supplier for type {} with key {}", type.getName(), contextKey);
    }

    @Override
    public T get() {
        log.atTrace().log("[ContextObjectSupplier] Getting object for key {} from execution context", contextKey);
        // This will be populated at runtime by the framework
        // For now, return null as this is a placeholder
        return null;
    }

    @Override
    public Class<T> getSuppliedType() {
        return type;
    }

    /**
     * Gets the context key for this supplier.
     *
     * @return the context key
     */
    public String getContextKey() {
        return contextKey;
    }
}

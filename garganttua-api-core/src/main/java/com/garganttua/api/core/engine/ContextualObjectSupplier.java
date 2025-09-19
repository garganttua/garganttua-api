package com.garganttua.api.core.engine;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.engine.IContextualObjectSupplier;
import com.garganttua.api.spec.engine.ISupplyObject;

public class ContextualObjectSupplier<Supplied, Context> implements IContextualObjectSupplier<Supplied, Context> {

    private Context context;
    private @Nonnull ISupplyObject<Supplied, Context> supply;
    private @Nonnull Class<Supplied> suppliedClass;
    private @Nonnull Class<Context> contextClass;

    public ContextualObjectSupplier(ISupplyObject<Supplied, Context> supply, Class<Supplied> suppliedClass,
            Class<Context> contextClass) {
        this.supply = Objects.requireNonNull(supply, "Supply cannot be null");
        this.suppliedClass = Objects.requireNonNull(suppliedClass, "Supplied class cannot be null");
        this.contextClass = Objects.requireNonNull(contextClass, "Supplied class cannot be null");
    }

    public void setContext(Context context) {
        this.context = Objects.requireNonNull(context, "Context cannot be null");
    }

    @Override
    public Optional<Supplied> getObject() {
        if (this.context == null) {
            return Optional.empty();
        }
        return this.supply.supplyObject(this.context);
    }

    @Override
    public Class<Supplied> getObjectClass() {
        return this.suppliedClass;
    }

    @Override
    public Class<Context> getContextClass() {
        return this.contextClass;
    }

}

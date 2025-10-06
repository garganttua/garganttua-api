package com.garganttua.api.core.context.application.supplier;

import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IExecutionContext;
import com.garganttua.api.spec.engine.ISupplyObject;

public class ApplicationContextObjectSupplier<Supplied>
        extends ContextualObjectSupplier<Supplied, IApplicationContext> {

    public ApplicationContextObjectSupplier(ISupplyObject<Supplied, IApplicationContext> supply,
            Class<Supplied> suppliedClass, Class<IApplicationContext> contextClass) {
        super(supply, suppliedClass, contextClass);
    }

    @Override
    public Optional<Supplied> getObject(IExecutionContext context) throws CoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
    }

    @Override
    public Optional<Supplied> getObject(IApplicationContext context) throws CoreException {
        this.context = Objects.requireNonNull(context, "Application context cannot be null");
        return this.getObject();
    }

}

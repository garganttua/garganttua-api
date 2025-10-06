package com.garganttua.api.core.context.application.supplier;

import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IExecutionContext;
import com.garganttua.api.spec.engine.ISupplyObject;

public class ExecutionContextObjectSupplier<Supplied> extends ContextualObjectSupplier<Supplied, IExecutionContext> {

    public ExecutionContextObjectSupplier(ISupplyObject<Supplied, IExecutionContext> supply,
            Class<Supplied> suppliedClass, Class<IExecutionContext> contextClass) {
        super(supply, suppliedClass, contextClass);
    }

    @Override
    public Optional<Supplied> getObject(IExecutionContext context) throws CoreException {
        this.context = Objects.requireNonNull(context, "Execution context cannot be null");
        return this.getObject();
    }

    @Override
    public Optional<Supplied> getObject(IApplicationContext context) throws CoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
    }

}

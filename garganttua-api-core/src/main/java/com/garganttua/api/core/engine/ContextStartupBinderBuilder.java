package com.garganttua.api.core.engine;

import java.util.Objects;

import com.garganttua.api.spec.engine.IContextBuilder;
import com.garganttua.api.spec.engine.IContextStartupBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplier;

public class ContextStartupBinderBuilder extends MethodBinderBuilder<IContextStartupBinderBuilder, Object, IContextBuilder> implements IContextStartupBinderBuilder {

    protected ContextStartupBinderBuilder(IContextBuilder up,
            IObjectSupplier<?> supplier) {
        super(up, supplier);
    }

     protected ContextStartupBinderBuilder(IContextBuilder up,
            Object object) {
        super(up, new ObjectSupplier<>(Objects.requireNonNull(object, "Object cannot be null")));
    }

    @Override
    protected ContextStartupBinderBuilder getReturned() {
        return this;
    }

}

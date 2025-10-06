package com.garganttua.api.core.context.application;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.garganttua.api.core.builder.binder.MethodBinderBuilder.NullableEnforcingSupplier;
import com.garganttua.api.core.context.application.supplier.ContextualObjectSupplier;
import com.garganttua.api.core.context.application.supplier.SupplyException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IExecutionContext;
import com.garganttua.api.spec.engine.IMethodBinder;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;

public class MethodBinder implements IMethodBinder {

    private @Nonnull IObjectSupplier<?> objectSupplier;
    private @Nonnull GGObjectAddress method;
    private @Nonnull List<IObjectSupplier<?>> parameterSuppliers;
    private boolean collection = false;

    public MethodBinder(IObjectSupplier<?> objectSupplier, GGObjectAddress method,
            List<IObjectSupplier<?>> parameterSuppliers) {
        this(objectSupplier, method, parameterSuppliers, false);
    }

    public MethodBinder(IObjectSupplier<?> objectSupplier, GGObjectAddress method,
            List<IObjectSupplier<?>> parameterSuppliers, boolean collection) {
        this.collection = collection;
        this.objectSupplier = Objects.requireNonNull(objectSupplier, "Object supplier cannot be null");
        this.method = Objects.requireNonNull(method, "Method cannot be null");
        this.parameterSuppliers = Objects.requireNonNull(parameterSuppliers, "Parameter suppliers cannot be null");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <Returned> Returned execute(IExecutionContext executionContext, IApplicationContext applicationContext)
            throws CoreException {

        Optional<?> target = this.objectSupplier.getObject(applicationContext, executionContext);
        if (target.isEmpty()) {
            throw new SupplyException("Target object supplier returned empty for method " + method);
        }

        try {
            Object[] args = buildArguments(executionContext, applicationContext);
            if (collection && Collection.class.isAssignableFrom(target.get().getClass())) {
                Collection<?> targets = (Collection) target.get();
                targets.forEach(t -> {
                    try {
                        GGObjectQueryFactory.objectQuery(t.getClass())
                                .invoke(t, this.method, args);
                    } catch (GGReflectionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });

                return null;
            } else {
                return (Returned) GGObjectQueryFactory.objectQuery(this.objectSupplier.getObjectClass())
                        .invoke(target.get(), this.method, args);
            }

        } catch (GGReflectionException e) {
            throw new SupplyException("Error invoking method " + method, e);
        }
    }

    private Object[] buildArguments(IExecutionContext execCtx, IApplicationContext appCtx) throws CoreException {
        if (this.parameterSuppliers.isEmpty()) {
            return new Object[0];
        }

        Object[] args = new Object[this.parameterSuppliers.size()];

        for (int i = 0; i < this.parameterSuppliers.size(); i++) {
            IObjectSupplier<?> supplier = this.parameterSuppliers.get(i);

            if (supplier instanceof NullableEnforcingSupplier<?> nes) {
                configureNullableSupplier(nes, execCtx, appCtx);
                args[i] = nes.getObject().orElse(null);

            } else if (supplier instanceof ContextualObjectSupplier<?, ?> cos) {
                /* configureContextualSupplier(cos, execCtx, appCtx); */
                args[i] = cos.getObject().orElse(null);

            } else {
                args[i] = supplier.getObject().orElse(null);
            }
        }

        return args;
    }

    private void configureNullableSupplier(NullableEnforcingSupplier<?> supplier,
            IExecutionContext execCtx,
            IApplicationContext appCtx) {
        Class<?> contextClass = supplier.isContextNeeded();
        if (contextClass == null)
            return;

        if (IApplicationContext.class.isAssignableFrom(contextClass)) {
            supplier.setApplicationContext(appCtx);
        }
        if (IExecutionContext.class.isAssignableFrom(contextClass)) {
            supplier.setExecutionContext(execCtx);
        }
    }

    @Override
    public <Returned> Returned execute() throws CoreException {
        return this.execute(null, null);
    }

}

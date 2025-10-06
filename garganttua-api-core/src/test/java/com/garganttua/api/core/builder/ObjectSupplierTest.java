package com.garganttua.api.core.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.builder.supplier.ApplicationContextObjectSupplierBuilder;
import com.garganttua.api.core.builder.supplier.ExecutionContextObjectSupplierBuilder;
import com.garganttua.api.core.builder.supplier.FixedObjectSupplierBuilder;
import com.garganttua.api.core.context.application.ApplicationContext;
import com.garganttua.api.core.context.execution.ExecutionContext;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IContextualObjectSupplier;
import com.garganttua.api.spec.engine.IExecutionContext;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.ISupplyObject;

public class ObjectSupplierTest {

    @Test
    public void testSimpleFixedStringSupplier() throws CoreException {

        IObjectSupplierBuilder<String> b = new IObjectSupplierBuilder<String>() {

            @Override
            public IObjectSupplier<String> build() throws CoreException {
                return new IObjectSupplier<String>() {

                    @Override
                    public Optional<String> getObject() {
                        return Optional.of("hello");
                    }

                    @Override
                    public Class<String> getObjectClass() {
                        return String.class;
                    }

                };
            }

            @Override
            public Class<String> getObjectClass() {
                return String.class;
            }

        };

        IObjectSupplier<String> supplier = (IObjectSupplier<String>) b.build();

        assertEquals("hello", supplier.getObject().get());
    }

    @Test
    public void testFixedObjectSupplier() throws CoreException {
        FixedObjectSupplierBuilder<String> builder = new FixedObjectSupplierBuilder<String>("hello");

        IObjectSupplier<String> supplier = builder.build();

        assertEquals("hello", supplier.getObject().get());
    }

    @Test
    public void testApplicationContextObjectSupplier() throws CoreException {

        ISupplyObject<String, IApplicationContext> supply = new ISupplyObject<String, IApplicationContext>() {

            @Override
            public Optional<String> supplyObject(IApplicationContext context) {
                return Optional.of("hello from context");
            }

        };

        ApplicationContextObjectSupplierBuilder<String> builder = new ApplicationContextObjectSupplierBuilder<String>(
                supply, String.class);

        IContextualObjectSupplier<String, IApplicationContext> supplier = (IContextualObjectSupplier<String, IApplicationContext>) builder.build();
        supplier.setContext(new ApplicationContext());

        assertEquals("hello from context", supplier.getObject().get());
        assertEquals(IApplicationContext.class, supplier.getContextClass());


    }

    @Test
    void testApplicationContextObjectLambdaSupplier() throws CoreException {

        ISupplyObject<String, IApplicationContext> supply = (context) -> Optional.of("hello from context");

        ApplicationContextObjectSupplierBuilder<String> builder = new ApplicationContextObjectSupplierBuilder<String>(
                supply, String.class);

        IContextualObjectSupplier<String, IApplicationContext> supplier = (IContextualObjectSupplier<String, IApplicationContext>) builder.build();
        supplier.setContext(new ApplicationContext());

        assertEquals("hello from context", supplier.getObject().get());
        assertEquals(IApplicationContext.class, supplier.getContextClass());
    }

    @Test
    void testExecutionContextObjectLambdaSupplier() throws CoreException {

        ExecutionContextObjectSupplierBuilder<String> builder = new ExecutionContextObjectSupplierBuilder<String>(
                (context) -> Optional.of("hello from context"), String.class);

        IContextualObjectSupplier<String, IExecutionContext> supplier = (IContextualObjectSupplier<String, IExecutionContext>) builder.build();
        supplier.setContext(new ExecutionContext());

        assertEquals("hello from context", supplier.getObject().get());
        assertEquals(IExecutionContext.class, supplier.getContextClass());

    }

        @Test
    void testExecutionContext() throws CoreException {

        ExecutionContextObjectSupplierBuilder<String> builder = new ExecutionContextObjectSupplierBuilder<String>(
                (context) -> Optional.of("hello from context"), String.class);

        IObjectSupplier<String> supplier = builder.build();

        assertTrue(IContextualObjectSupplier.class.isAssignableFrom(supplier.getClass()));

    }

}

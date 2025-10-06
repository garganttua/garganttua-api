package com.garganttua.api.core.engine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.AccessibleObject;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.builder.UseCaseBuilder;
import com.garganttua.api.core.builder.binder.UseCaseBinderBuilder;
import com.garganttua.api.core.builder.supplier.ApplicationContextObjectSupplierBuilder;
import com.garganttua.api.core.builder.supplier.FixedObjectSupplierBuilder;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IMethodBinder;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.ISupplyObject;
import com.garganttua.api.spec.engine.IUseCaseBinderBuilder;
import com.garganttua.reflection.fields.GGFields;

public class MethodBinderTest {

    public static class DummyMethodProvider {

        private Object echo(Object obj) {
            return obj;
        }
    }

    @Test
    public void shouldReturn5WhenExecutingStringBinderWithSize5() throws CoreException {

        IUseCaseBinderBuilder<?> builder = new UseCaseBinderBuilder<Object>(
                new UseCaseBuilder<Object>("test", new Object()), new FixedObjectSupplierBuilder<String>("Hello"))
                .method("length");

        IMethodBinder binder = builder.build();

        int size = binder.execute();

        assertEquals(5, size);

    }

    @Test
    public void shouldThrowExceptionWhenBuildButParametersNotSet() throws CoreException {
        IUseCaseBinderBuilder<?> builder = new UseCaseBinderBuilder<Object>(
                new UseCaseBuilder<Object>("test", new Object()),
                new FixedObjectSupplierBuilder<DummyMethodProvider>(new DummyMethodProvider()))
                .method("echo");

        CoreException exception = assertThrows(CoreException.class, () -> builder.build());
        assertEquals("Parameter 0 not configured for method echo", exception.getMessage());

    }

    @Test
    public void shouldReturn7WhenExecutingStringBinderWithSize7() throws CoreException {

        GGFields.BlackList.addClassToBlackList(AccessibleObject.class);
        GGFields.BlackList.addClassToBlackList(Record.class);
        GGFields.BlackList.addClassToBlackList(byte[].class);
        GGFields.BlackList.addClassToBlackList(Byte[].class);

        IUseCaseBinderBuilder<?> builder = new UseCaseBinderBuilder<Object>(
                new UseCaseBuilder<Object>("test", new Object()), new FixedObjectSupplierBuilder<String>("HelloHe"))
                .method("length");

        IMethodBinder binder = builder.build();

        int size = binder.execute();

        assertEquals(7, size);
    }

    @Test
    public void shouldTakeInAccountParameterSupplier() throws CoreException {

        IUseCaseBinderBuilder<?> builder = new UseCaseBinderBuilder<Object>(
                new UseCaseBuilder<Object>("test", new Object()),
                new FixedObjectSupplierBuilder<DummyMethodProvider>(new DummyMethodProvider()))
                .method("echo").withParam(new String("test-echo"));

        IMethodBinder binder = builder.build();

        String echo = binder.execute();

        assertEquals("test-echo", echo);
    }

    @Test
    public void shouldThrowAnErrorIfParameterSupplierSupplyNullButParameterNotNullable() throws CoreException {

        IUseCaseBinderBuilder<?> builder = new UseCaseBinderBuilder<Object>(
                new UseCaseBuilder<Object>("test", new Object()),
                new FixedObjectSupplierBuilder<DummyMethodProvider>(new DummyMethodProvider()))
                .method("echo").withParam(new IObjectSupplierBuilder<DummyMethodProvider>() {

                    @Override
                    public IObjectSupplier<DummyMethodProvider> build() throws CoreException {
                        return new IObjectSupplier<DummyMethodProvider>() {

                            @Override
                            public Optional<DummyMethodProvider> getObject() {
                                return Optional.empty();
                            }

                            @Override
                            public Class<DummyMethodProvider> getObjectClass() {
                                return DummyMethodProvider.class;
                            }

                        };
                    }

                    @Override
                    public Class<DummyMethodProvider> getObjectClass() {
                        return DummyMethodProvider.class;
                    }
                });

        IMethodBinder binder = builder.build();

        CoreException exception = assertThrows(CoreException.class, () -> binder.execute());

        assertEquals("Supplier for parameter 0 of method echo returned null but parameter is not nullable",
                exception.getMessage());
    }

    @Test
    public void shouldNotThrowAnErrorIfParameterSupplierSupplyNullButParameterNotNullable() throws CoreException {

        IUseCaseBinderBuilder<?> builder = new UseCaseBinderBuilder<Object>(
                new UseCaseBuilder<Object>("test", new Object()),
                new FixedObjectSupplierBuilder<DummyMethodProvider>(new DummyMethodProvider()))
                .method("echo").withParam(new IObjectSupplierBuilder<DummyMethodProvider>() {

                    @Override
                    public IObjectSupplier<DummyMethodProvider> build() throws CoreException {
                        return new IObjectSupplier<DummyMethodProvider>() {

                            @Override
                            public Optional<DummyMethodProvider> getObject() {
                                return Optional.empty();
                            }

                            @Override
                            public Class<DummyMethodProvider> getObjectClass() {
                                return DummyMethodProvider.class;
                            }

                        };
                    }

                    @Override
                    public Class<DummyMethodProvider> getObjectClass() {
                        return DummyMethodProvider.class;
                    }
                }, true);

        IMethodBinder binder = builder.build();

        assertDoesNotThrow(() -> {
            String echo = binder.execute();
            assertNull(echo);
        });
    }

    @Test
    public void shouldTakeInAccountContextIfNeeded() throws CoreException {

        ISupplyObject<Object, IApplicationContext> supply = new ISupplyObject<Object, IApplicationContext>() {

            @Override
            public Optional<Object> supplyObject(IApplicationContext context) {
                return Optional.of("test");
            }

        };

        ApplicationContextObjectSupplierBuilder<Object> supplierBuilder = new ApplicationContextObjectSupplierBuilder<>(
                supply, Object.class);

        IUseCaseBinderBuilder<?> builder = new UseCaseBinderBuilder<Object>(
                new UseCaseBuilder<Object>("test", new Object()),
                new FixedObjectSupplierBuilder<DummyMethodProvider>(new DummyMethodProvider()))
                .method("echo").withParam(supplierBuilder, false);

        IMethodBinder binder = builder.build();

        assertDoesNotThrow(() -> {
            String echo = binder.execute(null, null);
            assertEquals("test", echo);
        });
    }
}

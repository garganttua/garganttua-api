package com.garganttua.api.core.builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.builder.ApplicationContextBuilder;
import com.garganttua.api.core.builder.binder.ApplicationContextStartupBinderBuilder;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IApplicationContextBuilder;
import com.garganttua.api.spec.engine.IExecutionContext;
import com.garganttua.api.spec.engine.IObjectSupplier;

public class ContextStartupBinderBuilderTest {

    @Test
    public void testMethod() throws BuilderException {
        IApplicationContextBuilder builder = new ApplicationContextBuilder();
        ApplicationContextStartupBinderBuilder binderBuilder = new ApplicationContextStartupBinderBuilder(builder,
                new IObjectSupplier<Object>() {
                    @Override
                    public Optional<Object> getObject() {
                        return Optional.of(new Object());
                    }

                    @Override
                    public Class<Object> getObjectClass() {
                        return Object.class;
                    }

                    @Override
                    public Optional<Object> getObject(IApplicationContext aContext, IExecutionContext eContext)
                            throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }

                    @Override
                    public Optional<Object> getObject(IExecutionContext context) throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }

                    @Override
                    public Optional<Object> getObject(IApplicationContext context) throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }
                });

        assertDoesNotThrow(() -> {
            binderBuilder.method(Object.class.getDeclaredMethod("toString"));
        });
    }

    @Test
    public void testMethodWithName() throws BuilderException {
        IApplicationContextBuilder builder = new ApplicationContextBuilder();
        ApplicationContextStartupBinderBuilder binderBuilder = new ApplicationContextStartupBinderBuilder(builder,
                new IObjectSupplier<Object>() {
                    @Override
                    public Optional<Object> getObject() {
                        return Optional.of(new Object());
                    }

                    @Override
                    public Class<Object> getObjectClass() {
                        return Object.class;
                    }

                    @Override
                    public Optional<Object> getObject(IApplicationContext aContext, IExecutionContext eContext)
                            throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }

                    @Override
                    public Optional<Object> getObject(IExecutionContext context) throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }

                    @Override
                    public Optional<Object> getObject(IApplicationContext context) throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }
                });

        assertDoesNotThrow(() -> {
            binderBuilder.method("equals");
            binderBuilder.withParam(0, new Object());
        });
    }

    @Test
    public void testAddParameter() throws BuilderException {
        IApplicationContextBuilder builder = new ApplicationContextBuilder();
        ApplicationContextStartupBinderBuilder binderBuilder = new ApplicationContextStartupBinderBuilder(builder,
                new IObjectSupplier<Object>() {
                    @Override
                    public Optional<Object> getObject() {
                        return Optional.of(new Object());
                    }

                    @Override
                    public Class<Object> getObjectClass() {
                        return Object.class;
                    }

                    @Override
                    public Optional<Object> getObject(IApplicationContext aContext, IExecutionContext eContext)
                            throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }

                    @Override
                    public Optional<Object> getObject(IExecutionContext context) throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }

                    @Override
                    public Optional<Object> getObject(IApplicationContext context) throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }
                });

        assertDoesNotThrow(() -> {
            binderBuilder.method(Object.class.getDeclaredMethod("equals", Object.class));
            binderBuilder.withParam(0, new Object());
        });
    }

    @Test
    public void testNullParamsInCtor() {
        IApplicationContextBuilder builder = new ApplicationContextBuilder();
        NullPointerException exception1 = assertThrows(NullPointerException.class, () -> {
            new ApplicationContextStartupBinderBuilder(null,
                    new IObjectSupplier<Object>() {
                        @Override
                        public Optional<Object> getObject() {
                            return Optional.of(new Object());
                        }

                        @Override
                        public Class<Object> getObjectClass() {
                            return Object.class;
                        }

                        @Override
                        public Optional<Object> getObject(IApplicationContext aContext, IExecutionContext eContext)
                                throws CoreException {
                            // TODO Auto-generated method stub
                            throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                        }

                        @Override
                        public Optional<Object> getObject(IExecutionContext context) throws CoreException {
                            // TODO Auto-generated method stub
                            throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                        }

                        @Override
                        public Optional<Object> getObject(IApplicationContext context) throws CoreException {
                            // TODO Auto-generated method stub
                            throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                        }
                    });
        });
        NullPointerException exception2 = assertThrows(NullPointerException.class, () -> {
            new ApplicationContextStartupBinderBuilder(builder, null);
        });

        assertEquals("Up cannot be null", exception1.getMessage());
        assertEquals("Supplier cannot be null", exception2.getMessage());

    }

    @Test
    public void testWrongParamNumber() throws BuilderException {
        IApplicationContextBuilder builder = new ApplicationContextBuilder();
        ApplicationContextStartupBinderBuilder binderBuilder = new ApplicationContextStartupBinderBuilder(builder,
                new IObjectSupplier<Object>() {
                    @Override
                    public Optional<Object> getObject() {
                        return Optional.of(new Object());
                    }

                    @Override
                    public Class<Object> getObjectClass() {
                        return Object.class;
                    }

                    @Override
                    public Optional<Object> getObject(IApplicationContext aContext, IExecutionContext eContext)
                            throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }

                    @Override
                    public Optional<Object> getObject(IExecutionContext context) throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }

                    @Override
                    public Optional<Object> getObject(IApplicationContext context) throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }
                });

        CoreException exception = assertThrows(CoreException.class, () -> {
            binderBuilder
                    .method(Object.class.getDeclaredMethod("toString"))
                    .withParam(0, "null");
        });

        assertEquals("Method toString has only 0 parameters", exception.getMessage());
        assertEquals(CoreExceptionCode.BUILDER_CODE, exception.getCode());
    }

    @Test
    public void testWrongParamNumber2() throws BuilderException {
        IApplicationContextBuilder builder = new ApplicationContextBuilder();
        ApplicationContextStartupBinderBuilder binderBuilder = new ApplicationContextStartupBinderBuilder(builder,
                new IObjectSupplier<Object>() {
                    @Override
                    public Optional<Object> getObject() {
                        return Optional.of(new Object());
                    }

                    @Override
                    public Class<Object> getObjectClass() {
                        return Object.class;
                    }

                    @Override
                    public Optional<Object> getObject(IApplicationContext aContext, IExecutionContext eContext)
                            throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }

                    @Override
                    public Optional<Object> getObject(IExecutionContext context) throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }

                    @Override
                    public Optional<Object> getObject(IApplicationContext context) throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }
                });

        assertDoesNotThrow(() -> {
            binderBuilder
                    .method(Object.class.getDeclaredMethod("equals", Object.class))
                    .withParam(0, new Object());
        });

        CoreException exception = assertThrows(CoreException.class, () -> {
            binderBuilder.withParam(0, new Object());
        });

        assertEquals("Method equals has only 1 parameters", exception.getMessage());
        assertEquals(CoreExceptionCode.BUILDER_CODE, exception.getCode());
    }

    @Test
    public void testWrongParameterType() throws BuilderException {
        IApplicationContextBuilder builder = new ApplicationContextBuilder();
        ApplicationContextStartupBinderBuilder binderBuilder = new ApplicationContextStartupBinderBuilder(builder,
                new IObjectSupplier<String>() {
                    @Override
                    public Optional<String> getObject() {
                        return Optional.of("hello world");
                    }

                    @Override
                    public Class<String> getObjectClass() {
                        return String.class;
                    }

                    @Override
                    public Optional<String> getObject(IApplicationContext aContext, IExecutionContext eContext)
                            throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }

                    @Override
                    public Optional<String> getObject(IExecutionContext context) throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }

                    @Override
                    public Optional<String> getObject(IApplicationContext context) throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }
                });

        CoreException exception = assertThrows(CoreException.class, () -> {
            binderBuilder
                    .method(String.class.getDeclaredMethod("indexOf", String.class))
                    .withParam(0, new ApplicationContextBuilder());
        });

        assertEquals(
                "Parameter 0 of method indexOf is of type java.lang.String and cannot be assigned a value of type com.garganttua.api.core.engine.ContextBuilder",
                exception.getMessage());
        assertEquals(CoreExceptionCode.BUILDER_CODE, exception.getCode());
    }

    @Test
    public void testMethodFromAnotherType() throws BuilderException {
        IApplicationContextBuilder builder = new ApplicationContextBuilder();
        ApplicationContextStartupBinderBuilder binderBuilder = new ApplicationContextStartupBinderBuilder(builder,
                new IObjectSupplier<String>() {
                    @Override
                    public Optional<String> getObject() {
                        return Optional.of("hello world");
                    }

                    @Override
                    public Class<String> getObjectClass() {
                        return String.class;
                    }

                    @Override
                    public Optional<String> getObject(IApplicationContext aContext, IExecutionContext eContext)
                            throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }

                    @Override
                    public Optional<String> getObject(IExecutionContext context) throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }

                    @Override
                    public Optional<String> getObject(IApplicationContext context) throws CoreException {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
                    }
                });

        CoreException exception = assertThrows(CoreException.class, () -> {
            binderBuilder
                    .method(Object.class.getDeclaredMethod("equals", Object.class));
        });

        assertEquals("Method equals not found in class java.lang.String or does not match signature",
                exception.getMessage());
        assertEquals(CoreExceptionCode.BUILDER_CODE, exception.getCode());
    }

}

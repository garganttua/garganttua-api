package com.garganttua.api.core.engine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.engine.IContextBuilder;
import com.garganttua.api.spec.engine.IObjectSupplier;

public class ContextStartupBinderBuilderTest {

    @Test
    public void testMethod() {
        IContextBuilder builder = new ContextBuilder();
        ContextStartupBinderBuilder binderBuilder = new ContextStartupBinderBuilder(builder,
                new IObjectSupplier<Object>() {
                    @Override
                    public Object getObject() throws CoreException {
                        return new Object();
                    }

                    @Override
                    public Class<Object> getObjectClass() {
                        return Object.class;
                    }
                });

        assertDoesNotThrow(() -> {
            binderBuilder.method(Object.class.getDeclaredMethod("toString"));
        });
    }

    @Test
    public void testMethodWithName() {
        IContextBuilder builder = new ContextBuilder();
        ContextStartupBinderBuilder binderBuilder = new ContextStartupBinderBuilder(builder,
                new IObjectSupplier<Object>() {
                    @Override
                    public Object getObject() throws CoreException {
                        return new Object();
                    }

                    @Override
                    public Class<Object> getObjectClass() {
                        return Object.class;
                    }
                });

        assertDoesNotThrow(() -> {
            binderBuilder.method("equals");
            binderBuilder.withParam(0, new Object());
        });
    }

    @Test
    public void testAddParameter() {
        IContextBuilder builder = new ContextBuilder();
        ContextStartupBinderBuilder binderBuilder = new ContextStartupBinderBuilder(builder,
                new IObjectSupplier<Object>() {
                    @Override
                    public Object getObject() throws CoreException {
                        return new Object();
                    }

                    @Override
                    public Class<Object> getObjectClass() {
                        return Object.class;
                    }
                });

        assertDoesNotThrow(() -> {
            binderBuilder.method(Object.class.getDeclaredMethod("equals", Object.class));
            binderBuilder.withParam(0, new Object());
        });
    }

    @Test
    public void testNullParamsInCtor() {
        IContextBuilder builder = new ContextBuilder();
        NullPointerException exception1 = assertThrows(NullPointerException.class, () -> {
            new ContextStartupBinderBuilder(null,
                    new IObjectSupplier<Object>() {
                        @Override
                        public Object getObject() throws CoreException {
                            return new Object();
                        }

                        @Override
                        public Class<Object> getObjectClass() {
                            return Object.class;
                        }
                    });
        });
        NullPointerException exception2 = assertThrows(NullPointerException.class, () -> {
            new ContextStartupBinderBuilder(builder, null);
        });

        assertEquals("Up cannot be null", exception1.getMessage());
        assertEquals("Supplier cannot be null", exception2.getMessage());

    }

    @Test
    public void testWrongParamNumber() {
        IContextBuilder builder = new ContextBuilder();
        ContextStartupBinderBuilder binderBuilder = new ContextStartupBinderBuilder(builder,
                new IObjectSupplier<Object>() {
                    @Override
                    public Object getObject() throws CoreException {
                        return new Object();
                    }

                    @Override
                    public Class<Object> getObjectClass() {
                        return Object.class;
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
    public void testWrongParamNumber2() {
        IContextBuilder builder = new ContextBuilder();
        ContextStartupBinderBuilder binderBuilder = new ContextStartupBinderBuilder(builder,
                new IObjectSupplier<Object>() {
                    @Override
                    public Object getObject() throws CoreException {
                        return new Object();
                    }

                    @Override
                    public Class<Object> getObjectClass() {
                        return Object.class;
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
    public void testWrongParameterType() {
        IContextBuilder builder = new ContextBuilder();
        ContextStartupBinderBuilder binderBuilder = new ContextStartupBinderBuilder(builder,
                new IObjectSupplier<String>() {
                    @Override
                    public String getObject() throws CoreException {
                        return "hello world";
                    }

                    @Override
                    public Class<String> getObjectClass() {
                        return String.class;
                    }
                });

       CoreException exception = assertThrows(CoreException.class, () -> {
            binderBuilder
                    .method(String.class.getDeclaredMethod("indexOf", String.class))
                    .withParam(0, new ContextBuilder());
        });

        assertEquals("Parameter 0 of method indexOf is of type java.lang.String and cannot be assigned a value of type com.garganttua.api.core.engine.ContextBuilder", exception.getMessage());
        assertEquals(CoreExceptionCode.BUILDER_CODE, exception.getCode());
    }

     @Test
    public void testMethodFromAnotherType() {
        IContextBuilder builder = new ContextBuilder();
        ContextStartupBinderBuilder binderBuilder = new ContextStartupBinderBuilder(builder,
                new IObjectSupplier<String>() {
                    @Override
                    public String getObject() throws CoreException {
                        return "hello world";
                    }

                    @Override
                    public Class<String> getObjectClass() {
                        return String.class;
                    }
                });

       CoreException exception = assertThrows(CoreException.class, () -> {
            binderBuilder
                    .method(Object.class.getDeclaredMethod("equals", Object.class));
        });

        assertEquals("Method equals not found in class java.lang.String or does not match signature", exception.getMessage());
        assertEquals(CoreExceptionCode.BUILDER_CODE, exception.getCode());
    }

}

package com.garganttua.api.core.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.builder.binder.MethodBinderBuilder;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IMethodBinder;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;

class TestClass {
    public void noParamMethod() {}
    public void oneParamMethod(String arg) {}
    public void twoParamsMethod(String a, Integer b) {}
}

// Implémentation concrète minimale pour tester
class TestMethodBinderBuilder extends MethodBinderBuilder<TestMethodBinderBuilder, Object> {
    protected TestMethodBinderBuilder(Object up, IObjectSupplierBuilder<?> supplier) throws BuilderException {
        super(up, supplier);
    }

    @Override
    protected TestMethodBinderBuilder getReturned() {
        return this;
    }

}

public class MethodBinderBuilderTest {

    private IObjectSupplierBuilder<TestClass> supplier;
    private TestMethodBinderBuilder builder;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws CoreException {
        supplier = mock(IObjectSupplierBuilder.class);
        when(supplier.getObjectClass()).thenReturn(TestClass.class);
        builder = new TestMethodBinderBuilder("UP", supplier);
    }

    @Test
    void testMethodFound() throws Exception {
        Method m = TestClass.class.getDeclaredMethod("noParamMethod");
        builder.method(m);
        assertNotNull(builder);
    }

    @Test
    void testMethodNotFound() throws Exception {
        Method m = Object.class.getDeclaredMethod("toString");
        assertThrows(CoreException.class, () -> builder.method(m));
    }

    @Test
    void testWithParamValidObject() throws Exception {
        Method m = TestClass.class.getDeclaredMethod("oneParamMethod", String.class);
        builder.method(m);
        builder.withParam(0, "Hello");
    }

    @Test
    void testWithParamInvalidIndex() throws Exception {
        Method m = TestClass.class.getDeclaredMethod("oneParamMethod", String.class);
        builder.method(m);
        assertThrows(CoreException.class, () -> builder.withParam(5, "test"));
    }

    @Test
    void testWithParamInvalidType() throws Exception {
        Method m = TestClass.class.getDeclaredMethod("oneParamMethod", String.class);
        builder.method(m);
        assertThrows(CoreException.class, () -> builder.withParam(0, 42));
    }

    @Test
    void testWithParamSupplierValid() throws Exception {
        Method m = TestClass.class.getDeclaredMethod("oneParamMethod", String.class);
        builder.method(m);

        @SuppressWarnings("unchecked")
        IObjectSupplier<String> paramSupplier = mock(IObjectSupplier.class);
        when(paramSupplier.getObjectClass()).thenReturn(String.class);

        builder.withParam(0, paramSupplier);
    }

    @Test
    void testWithParamSupplierInvalidType() throws Exception {
        Method m = TestClass.class.getDeclaredMethod("oneParamMethod", String.class);
        builder.method(m);

        @SuppressWarnings("unchecked")
        IObjectSupplier<Integer> paramSupplier = mock(IObjectSupplier.class);
        when(paramSupplier.getObjectClass()).thenReturn(Integer.class);

        assertThrows(CoreException.class, () -> builder.withParam(0, paramSupplier));
    }

    @Test
    void testUp() {
        assertEquals("UP", builder.up());
    }

    @Test
    void testBuild() throws Exception {
        IMethodBinder methodBinder = builder.build();
        
        assertNotNull(methodBinder);
    }
}

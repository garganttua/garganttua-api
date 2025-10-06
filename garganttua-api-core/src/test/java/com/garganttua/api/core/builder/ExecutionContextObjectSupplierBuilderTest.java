package com.garganttua.api.core.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.builder.supplier.ExecutionContextObjectSupplierBuilder;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IExecutionContext;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.api.spec.engine.ISupplyObject;

class ExecutionContextObjectSupplierBuilderTest {

    @Test
    @DisplayName("Construction avec supply et suppliedClass valides doit réussir")
    void testConstructorValidArgs() {
        ISupplyObject<String, IExecutionContext> supply = ctx -> Optional.of("hello");

        ExecutionContextObjectSupplierBuilder<String> builder =
                new ExecutionContextObjectSupplierBuilder<>(supply, String.class);

        assertNotNull(builder);
        assertEquals(String.class, builder.getObjectClass());
    }

    @Test
    @DisplayName("Construction avec supply null doit échouer")
    void testConstructorNullSupply() {
        assertThrows(NullPointerException.class,
                () -> new ExecutionContextObjectSupplierBuilder<>(null, String.class));
    }

    @Test
    @DisplayName("Construction avec suppliedClass null doit échouer")
    void testConstructorNullSuppliedClass() {
        ISupplyObject<String, IExecutionContext> supply = ctx -> Optional.of("value");

        assertThrows(NullPointerException.class,
                () -> new ExecutionContextObjectSupplierBuilder<>(supply, null));
    }

    @Test
    @DisplayName("build() retourne un ContextualObjectSupplier fonctionnel")
    void testBuildReturnsContextualSupplier() throws CoreException {
        ISupplyObject<Integer, IExecutionContext> supply = ctx -> Optional.of(99);

        ExecutionContextObjectSupplierBuilder<Integer> builder =
                new ExecutionContextObjectSupplierBuilder<>(supply, Integer.class);

        IObjectSupplier<Integer> supplier = builder.build();

        assertNotNull(supplier);
        assertEquals(Integer.class, supplier.getObjectClass());

        // On suppose que ContextualObjectSupplier appelle bien supply.getObject(context)
        Optional<Integer> result = supplier.getObject();
        assertTrue(result.isPresent());
        assertEquals(99, result.get());
    }

    @Test
    @DisplayName("getObjectClass() retourne la classe fournie au constructeur")
    void testGetObjectClass() {
        ISupplyObject<Double, IExecutionContext> supply = ctx -> Optional.of(3.14);

        ExecutionContextObjectSupplierBuilder<Double> builder =
                new ExecutionContextObjectSupplierBuilder<>(supply, Double.class);

        assertEquals(Double.class, builder.getObjectClass());
    }
}

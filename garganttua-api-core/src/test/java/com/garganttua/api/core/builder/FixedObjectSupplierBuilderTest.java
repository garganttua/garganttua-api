package com.garganttua.api.core.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.builder.supplier.FixedObjectSupplierBuilder;
import com.garganttua.api.spec.engine.IObjectSupplier;

class FixedObjectSupplierBuilderTest {

    @Test
    @DisplayName("Construction avec un objet non null doit réussir")
    void testConstructorWithNonNullObject() {
        String value = "hello";
        FixedObjectSupplierBuilder<String> builder = new FixedObjectSupplierBuilder<>(value);

        assertNotNull(builder);
        assertEquals(String.class, builder.getObjectClass());
    }

    @Test
    @DisplayName("Construction avec un objet null doit échouer")
    void testConstructorWithNullObject() {
        assertThrows(NullPointerException.class, () -> new FixedObjectSupplierBuilder<>(null));
    }

    @Test
    @DisplayName("build() retourne un IObjectSupplier non null")
    void testBuildReturnsSupplier() throws Exception {
        Integer value = 42;
        FixedObjectSupplierBuilder<Integer> builder = new FixedObjectSupplierBuilder<>(value);

        IObjectSupplier<Integer> supplier = builder.build();

        assertNotNull(supplier);
        assertEquals(Integer.class, supplier.getObjectClass());
    }

    @Test
    @DisplayName("getObject() du supplier retourne la bonne valeur")
    void testGetObject() throws Exception {
        String value = "test-value";
        FixedObjectSupplierBuilder<String> builder = new FixedObjectSupplierBuilder<>(value);

        IObjectSupplier<String> supplier = builder.build();
        Optional<String> result = supplier.getObject();

        assertTrue(result.isPresent());
        assertEquals(value, result.get());
    }

    @Test
    @DisplayName("getObjectClass() du builder et du supplier sont identiques")
    void testGetObjectClassConsistency() throws Exception {
        Double value = 3.14;
        FixedObjectSupplierBuilder<Double> builder = new FixedObjectSupplierBuilder<>(value);

        IObjectSupplier<Double> supplier = builder.build();

        assertEquals(builder.getObjectClass(), supplier.getObjectClass());
    }
}
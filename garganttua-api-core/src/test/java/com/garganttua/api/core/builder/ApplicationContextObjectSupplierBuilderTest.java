package com.garganttua.api.core.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.builder.supplier.ApplicationContextObjectSupplierBuilder;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.api.spec.engine.ISupplyObject;

class ApplicationContextObjectSupplierBuilderTest {

    @Test
    @DisplayName("Construction avec supply et suppliedClass valides doit réussir")
    void testConstructorValidArgs() {
        ISupplyObject<String, IApplicationContext> supply = ctx -> Optional.of("hello");

        ApplicationContextObjectSupplierBuilder<String> builder =
                new ApplicationContextObjectSupplierBuilder<>(supply, String.class);

        assertNotNull(builder);
        assertEquals(String.class, builder.getObjectClass());
    }

    @Test
    @DisplayName("Construction avec supply null doit échouer")
    void testConstructorNullSupply() {
        assertThrows(NullPointerException.class,
                () -> new ApplicationContextObjectSupplierBuilder<>(null, String.class));
    }

    @Test
    @DisplayName("Construction avec suppliedClass null doit échouer")
    void testConstructorNullSuppliedClass() {
        ISupplyObject<String, IApplicationContext> supply = ctx -> Optional.of("value");

        assertThrows(NullPointerException.class,
                () -> new ApplicationContextObjectSupplierBuilder<>(supply, null));
    }

    @Test
    @DisplayName("build() retourne un ContextualObjectSupplier fonctionnel")
    void testBuildReturnsContextualSupplier() throws CoreException {
        ISupplyObject<Integer, IApplicationContext> supply = ctx -> Optional.of(123);

        ApplicationContextObjectSupplierBuilder<Integer> builder =
                new ApplicationContextObjectSupplierBuilder<>(supply, Integer.class);

        IObjectSupplier<Integer> supplier = builder.build();

        assertNotNull(supplier);
        assertEquals(Integer.class, supplier.getObjectClass());

        Optional<Integer> result = supplier.getObject();
        assertTrue(result.isPresent());
        assertEquals(123, result.get());
    }

    @Test
    @DisplayName("getObjectClass() retourne la classe fournie au constructeur")
    void testGetObjectClass() {
        ISupplyObject<Double, IApplicationContext> supply = ctx -> Optional.of(3.14);

        ApplicationContextObjectSupplierBuilder<Double> builder =
                new ApplicationContextObjectSupplierBuilder<>(supply, Double.class);

        assertEquals(Double.class, builder.getObjectClass());
    }
}
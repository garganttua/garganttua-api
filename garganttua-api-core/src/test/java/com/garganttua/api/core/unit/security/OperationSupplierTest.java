package com.garganttua.api.core.unit.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;

import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.core.security.authentication.OperationSupplier;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.SupplyException;

@DisplayName("OperationSupplier Tests")
class OperationSupplierTest {

    @BeforeAll
    static void initReflection() {
        IClass.setReflection(ReflectionBuilder.builder()
                .withProvider(new RuntimeReflectionProvider())
                .withScanner(new ReflectionsAnnotationScanner())
                .build());
    }

    private OperationSupplier supplier;
    @SuppressWarnings("rawtypes")
    private IRuntimeContext runtimeContext;
    private IOperationRequest operationRequest;

    @BeforeEach
    void setUp() {
        supplier = new OperationSupplier();
        runtimeContext = mock(IRuntimeContext.class);
        operationRequest = mock(IOperationRequest.class);
    }

    @Nested
    @DisplayName("Type metadata")
    class TypeMetadata {

        @Test
        @DisplayName("getSuppliedType returns OperationDefinition type")
        void suppliedTypeIsOperationDefinition() {
            assertEquals(OperationDefinition.class, supplier.getSuppliedType());
        }

        @Test
        @DisplayName("getOwnerContextType wraps IRuntimeContext.class")
        void ownerContextTypeWrapsRuntimeContext() {
            assertEquals(IRuntimeContext.class, supplier.getOwnerContextType().getType());
        }
    }

    @Nested
    @DisplayName("supply() validation")
    class SupplyValidation {

        @Test
        @DisplayName("throws when context is null")
        void throwsOnNullContext() {
            assertThrows(SupplyException.class, () -> supplier.supply(null));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("throws when request variable is missing")
        void throwsOnMissingRequest() {
            when(runtimeContext.getVariable(eq("request"), any(IClass.class))).thenReturn(Optional.empty());
            assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
        }
    }

    @Nested
    @DisplayName("supply() with valid input")
    class SupplyWithValidInput {

        @SuppressWarnings("unchecked")
        @BeforeEach
        void setUpVariables() {
            when(runtimeContext.getVariable(eq("request"), any(IClass.class))).thenReturn(Optional.of(operationRequest));
        }

        @Test
        @DisplayName("returns operation from request")
        void returnsOperation() throws SupplyException {
            OperationDefinition operation = mock(OperationDefinition.class);
            when(operationRequest.operation()).thenReturn(operation);

            Optional<OperationDefinition> result = supplier.supply(runtimeContext);

            assertTrue(result.isPresent());
            assertSame(operation, result.get());
        }

        @Test
        @DisplayName("returns empty when operation is null")
        void returnsEmptyWhenOperationNull() throws SupplyException {
            when(operationRequest.operation()).thenReturn(null);

            Optional<OperationDefinition> result = supplier.supply(runtimeContext);

            assertTrue(result.isEmpty());
        }
    }
}

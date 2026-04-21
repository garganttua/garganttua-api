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

import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.core.security.authentication.ApiSupplier;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.SupplyException;

@DisplayName("ApiSupplier Tests")
class ApiSupplierTest {

    @BeforeAll
    static void initReflection() {
        IClass.setReflection(ReflectionBuilder.builder()
                .withProvider(new RuntimeReflectionProvider())
                .withScanner(new ReflectionsAnnotationScanner())
                .build());
    }

    private ApiSupplier supplier;
    @SuppressWarnings("rawtypes")
    private IRuntimeContext runtimeContext;
    private IOperationRequest operationRequest;
    private IApi apiContext;

    @BeforeEach
    void setUp() {
        supplier = new ApiSupplier();
        runtimeContext = mock(IRuntimeContext.class);
        operationRequest = mock(IOperationRequest.class);
        apiContext = mock(IApi.class);
    }

    @Nested
    @DisplayName("Type metadata")
    class TypeMetadata {

        @Test
        @DisplayName("getSuppliedType returns IApi type")
        void suppliedTypeIsApi() {
            assertEquals(IApi.class, supplier.getSuppliedType());
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
        @DisplayName("returns apiContext from request")
        void returnsApi() throws SupplyException {
            when(operationRequest.arg(IOperationRequest.API_CONTEXT)).thenReturn(Optional.of(apiContext));

            Optional<IApi> result = supplier.supply(runtimeContext);

            assertTrue(result.isPresent());
            assertSame(apiContext, result.get());
        }

        @Test
        @DisplayName("throws when apiContext is missing from request")
        void throwsWhenApiMissing() {
            when(operationRequest.arg(IOperationRequest.API_CONTEXT)).thenReturn(Optional.empty());

            assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
        }
    }
}
